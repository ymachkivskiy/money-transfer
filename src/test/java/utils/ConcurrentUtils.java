package utils;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.Collectors.toList;

public class ConcurrentUtils {

    public static void performConcurrentWork(Collection<Runnable> workers) throws InterruptedException {

        ExecutorService executorService = newFixedThreadPool(workers.size(), new DefaultThreadFactory("test-worker-"));

        try {

            final CountDownLatch start = new CountDownLatch(1);
            final CountDownLatch end = new CountDownLatch(workers.size());

            for (Runnable w : workers) {
                executorService.execute(() -> {
                    try {
                        start.await();

                        w.run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        end.countDown();
                    }
                });
            }

            start.countDown();
            end.await();

        } finally {
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        }

    }

    public static Collection<Runnable> duplicateWorker(int count, Runnable single) {
        return IntStream.range(0, count)
                .mapToObj(i -> single)
                .collect(toList());
    }

    public static Runnable multiplyAction(int multiplier, Runnable action) {
        checkArgument(multiplier > 0);
        return () -> {
            for (int i = 0; i < multiplier; i++) {
                action.run();
            }
        };
    }

}
