## Overview

A simple JUnit rule and annotation to allow executing JUnit tests concurrently.

## Usage

```java
import org.junit.Rule;
import org.junit.Test;
import org.co.instil.junit.performance.BenchmarkRule;
import org.co.instil.junit.performance.ExecuteConcurrently;

public class PerformanceTest {

    @Rule
    public BenchmarkRule benchmarkRule = new BenchmarkRule();
    
    @Test
    @ExecuteConcurrently(iterations = 50000, threads = 10)
    public void shouldTestSomeBehaviour() throws Exception {
        // test implementation
    }

}
 ```