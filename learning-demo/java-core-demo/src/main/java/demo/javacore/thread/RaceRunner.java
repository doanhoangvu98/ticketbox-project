package demo.javacore.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntPredicate;

/**
 * Runs the same task on many threads that all start at the same moment, and counts the successes.
 *
 * <ul>
 *   <li>ExecutorService: a pool of reusable threads, so we do not create and start threads by hand.</li>
 *   <li>CountDownLatch "startGate": every task waits until all are submitted, then they start together.
 *       Without it the first tasks would finish before the last ones even start (no real contention).</li>
 *   <li>AtomicInteger: a counter that many threads can increment safely
 *       ({@code count++} on a plain int is read-modify-write and would lose updates).</li>
 * </ul>
 */
public final class RaceRunner {

    private static final int POOL_SIZE = 32;

    private RaceRunner() {
    }

    public static int run(int tasks, IntPredicate task) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(tasks);
        AtomicInteger successes = new AtomicInteger();
        try {
            for (int i = 0; i < tasks; i++) {
                int taskId = i; // a lambda can only capture (effectively) final variables
                pool.submit(() -> {
                    try {
                        startGate.await();
                        if (task.test(taskId)) {
                            successes.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finished.countDown(); // always, even if the task threw
                    }
                });
            }
            startGate.countDown(); // release all tasks at once
            if (!finished.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Tasks did not finish in time");
            }
            return successes.get();
        } finally {
            pool.shutdownNow();
        }
    }
}
