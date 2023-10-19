package virtualthreads;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

public class ProducerConsumerExecutor {
    static int getRandomTime(Random random) {
        return random.ints(500, 1000).findFirst().getAsInt();
    }

    public static void main(String[] args) {
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            BlockingQueue<String> queue = new LinkedBlockingQueue<>();
            Random random = new Random();

            IntStream.range(1, 11).forEach(id -> {
                Runnable producer = (() -> {
                    while (true) {
                        try {
                            Thread.sleep(getRandomTime(random));

                            var value = RandomStringUtils.randomAlphanumeric(10);
                            queue.put(value);
                            System.out.println("producer " + id + " producing " + value);
                        } catch (InterruptedException e) {
                            System.out.println("producer " + id + " error producing value " + e.getMessage());
                        }
                    }
                });

                executorService.execute(producer);
            });

            IntStream.range(1, 11).forEach(id -> {
                Runnable consumer = (() -> {
                    while (true) {
                        try {
                            Thread.sleep(getRandomTime(random));

                            var value = queue.take();
                            System.out.println("consumer " + id + " consuming value " + value);
                        } catch (InterruptedException e) {
                            System.out.println("consumer " + id + " error consuming value " + e.getMessage());
                        }
                    }
                });

                executorService.execute(consumer);
            });
        }
    }
}
