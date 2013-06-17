package co.instil.junit.performance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a test method as being eligible for concurrent execution.
 * Both the number of iterations and size of thread pool must be provided.
 *
 * <pre>
 * {@code
 * @org.junit.Test
 * @ExecuteConcurrently(iterations = 50000, threads = 10);
 * public void shouldTestSomeBehaviour() throws Exception {
 *     // test implementation
 * }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ExecuteConcurrently {

    int iterations();
    int threads();

}