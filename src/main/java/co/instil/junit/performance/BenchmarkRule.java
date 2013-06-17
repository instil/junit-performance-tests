package co.instil.junit.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static java.lang.String.format;

/**
 * A JUnit rule which executes tests concurrently and outputs
 * elapsed time and average time per test execution.
 *
 * <pre>
 * {@code
 * @org.junit.Rule
 * public BenchmarkRule benchmarkRule = new BenchmarkRule();
 * }
 * </pre>
 */
public class BenchmarkRule extends TestWatcher {

    private int iterations;
    private int threads;
    private long average;
    private long startTimeInMillis;

    @Override
    protected void starting(Description description) {
        startTimeInMillis = System.currentTimeMillis();
    }

    @Override
    protected void finished(Description description) {
        System.out.println(format("%s - %d threads executing %d iterations", testNameFrom(description), threads, iterations));
        long duration = System.currentTimeMillis() - startTimeInMillis;
        System.out.println(format("Average time per iteration %sms, elapsed time %dms", average, duration));
    }

    private String testNameFrom(Description description) {
        return description.getClassName() + "." + description.getMethodName();
    }

    @Override
    public Statement apply(final Statement statement, final Description description) {
        Statement wrappedStatement = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                executeTest(statement, description);
            }
        };
        return super.apply(wrappedStatement, description);
    }

    private void executeTest(Statement statement, Description description) throws Exception {
        ExecuteConcurrently concurrent = description.getAnnotation(ExecuteConcurrently.class);
        if (concurrent == null) {
            threads = iterations = 1;
            average = executeTestSequentially(statement);
        } else {
            iterations = concurrent.iterations();
            threads = concurrent.threads();
            long totalTime = executeTestConcurrently(statement, iterations, threads);
            average = totalTime / iterations;
        }
    }

    private long executeTestSequentially(Statement statement) {
        long startTimeInMillis = System.currentTimeMillis();
        try {
            statement.evaluate();
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        return System.currentTimeMillis() - startTimeInMillis;
    }

    private long executeTestConcurrently(Statement statement, int iterations, int threads) throws Exception {
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(iterations);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(threads, threads, 0, TimeUnit.MILLISECONDS, workQueue);
        List<Callable<Long>> tasks = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            tasks.add(newCallableFor(statement));
        }
        List<Future<Long>> results = executor.invokeAll(tasks);
        long totalTime = 0;
        for (Future<Long> result : results) {
            totalTime += result.get();
        }
        return totalTime;
    }

    private Callable<Long> newCallableFor(final Statement statement) {
        return new Callable<Long>() {
            @Override
            public Long call() {
                return executeTestSequentially(statement);
            }
        };
    }

}