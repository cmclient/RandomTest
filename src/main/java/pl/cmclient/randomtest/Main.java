package pl.cmclient.randomtest;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    private static final int TESTS = 1_000_000;
    private static final float CHANCE = 0.01F;
    private static final AtomicInteger firstTrySuccessCount = new AtomicInteger(0);
    private static final AtomicInteger minTries = new AtomicInteger(Integer.MAX_VALUE);
    private static final AtomicInteger maxTries = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis(); // Record the start time
        Thread[] threads = new Thread[NUM_THREADS];

        System.out.println("-- RANDOMNESS TEST --");
        System.out.printf("Threads: %d\n", NUM_THREADS);
        System.out.printf("Tests: %d\n", TESTS);
        System.out.println("------------------------------");

        for (int t = 0; t < NUM_THREADS; t++) {
            threads[t] = new Thread(() -> {
                int[] threadLocalResults = new int[3];

                for (int i = 0; i < TESTS / NUM_THREADS; i++) {
                    int tries = 0;

                    do {
                        tries++;
                        if (RANDOM.nextFloat() < CHANCE) {
                            if (tries == 1) {
                                threadLocalResults[0]++;
                            }
                            break;
                        }
                    } while (true);

                    threadLocalResults[1] = Math.min(threadLocalResults[1], tries);
                    threadLocalResults[2] = Math.max(threadLocalResults[2], tries);
                }

                accumulateResults(threadLocalResults);
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        double averageTries = (minTries.get() + maxTries.get()) / 2.0; // Calculate average of min and max
        long endTime = System.currentTimeMillis(); // Record the end time
        double elapsedTime = (endTime - startTime) / 1000.0; // Calculate elapsed time in seconds

        System.out.println("Randomness Testing Results:");
        System.out.println("------------------------------");
        System.out.printf("Minimum tries: %d\n", minTries.get());
        System.out.printf("Average tries: %.2f\n", averageTries);
        System.out.printf("Maximum tries: %d\n", maxTries.get());
        System.out.printf("First try success count: %d\n", firstTrySuccessCount.get());
        System.out.printf("Completed after %.2f seconds\n", elapsedTime);
    }

    private static void accumulateResults(int[] threadLocalResults) {
        firstTrySuccessCount.addAndGet(threadLocalResults[0]);
        minTries.updateAndGet(value -> Math.min(value, threadLocalResults[1]));
        maxTries.updateAndGet(value -> Math.max(value, threadLocalResults[2]));
    }
}
