package virtualthreads;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

public class ProducerConsumer {
    static int getRandomTime(Random random) {
        return random.ints(500, 1000).findFirst().getAsInt();
    }

    public static void main(String[] args) throws InterruptedException {
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

            var task = Thread.ofVirtual().unstarted(producer);
            task.start();
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

            var task = Thread.ofVirtual().unstarted(consumer);
            task.start();
        });

        Thread.sleep(30000);
    }
}
