package net.ruj.cloudfuse.samples;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class CompletableFutureTest {
    @Test
    public void supplierTwiceGetTest() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(new TestSupplier(), executor);
        String firstExecutionResult = completableFuture.get();
        String secondExecutionResult = completableFuture.get();
        Assertions.assertThat(firstExecutionResult).isEqualTo(secondExecutionResult);
    }

    class TestSupplier implements Supplier<String> {
        private boolean executed = false;

        @Override
        public String get() {
            if (!executed) {
                executed = true;
                return "Hello World";
            } else
                return "";
        }
    }
}
