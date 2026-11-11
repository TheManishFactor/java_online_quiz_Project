package com.onlinequiz.threads;

import java.util.LinkedList;

/**
 * Console demonstration of Java multithreading concepts used by the quiz timer.
 *
 * Run with:
 * java -cp "target/classes" com.onlinequiz.threads.ThreadDemo
 */
public final class ThreadDemo {

    private ThreadDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        demonstrateThreadSubclass();
        demonstrateRunnableAndPriorities();
        demonstrateLifecycle();
        demonstrateSynchronization();
        demonstrateWaitNotify();
        System.out.println();
        System.out.println("All thread demonstrations finished cleanly.");
    }

    private static void demonstrateThreadSubclass() throws InterruptedException {
        System.out.println("1. CREATING A THREAD WITH Thread SUBCLASS");
        CounterThread worker = new CounterThread("thread-subclass");
        worker.start();
        worker.join();
        System.out.println("   worker finished");
    }

    private static void demonstrateRunnableAndPriorities() throws InterruptedException {
        System.out.println();
        System.out.println("2. CREATING A THREAD WITH Runnable AND SETTING PRIORITY");
        Runnable job = () -> System.out.println("   runnable is running on "
                + Thread.currentThread().getName());
        Thread worker = new Thread(job, "runnable-worker");
        worker.setPriority(Thread.MAX_PRIORITY);
        System.out.println("   priority hint : " + worker.getPriority()
                + " (MIN " + Thread.MIN_PRIORITY + ", MAX " + Thread.MAX_PRIORITY + ")");
        worker.start();
        worker.join();
        System.out.println("   priority is a scheduling hint, not a guarantee");
    }

    private static void demonstrateLifecycle() throws InterruptedException {
        System.out.println();
        System.out.println("3. THREAD LIFECYCLE");
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(150);
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            }
        }, "lifecycle-worker");

        System.out.println("   after creation : " + worker.getState());
        worker.start();
        System.out.println("   after start()   : " + worker.getState());
        worker.join();
        System.out.println("   after join()    : " + worker.getState());
        System.out.println("   states shown    : NEW -> RUNNABLE -> TERMINATED");
    }

    private static void demonstrateSynchronization() throws InterruptedException {
        System.out.println();
        System.out.println("4. SYNCHRONIZATION");
        SynchronizedCounter counter = new SynchronizedCounter();
        Thread[] workers = new Thread[4];
        for (int index = 0; index < workers.length; index++) {
            workers[index] = new Thread(() -> {
                for (int step = 0; step < 500; step++) {
                    counter.increment();
                }
            }, "counter-" + (index + 1));
            workers[index].start();
        }
        for (Thread worker : workers) {
            worker.join();
        }
        System.out.println("   4 threads x 500 increments = " + counter.get()
                + " (synchronized keeps the total exact)");
    }

    private static void demonstrateWaitNotify() throws InterruptedException {
        System.out.println();
        System.out.println("5. INTER-THREAD COMMUNICATION WITH wait() / notifyAll()");
        MessageBox box = new MessageBox(2);

        Thread producer = new Thread(() -> {
            try {
                box.put("message-1");
                box.put("message-2");
                box.put("message-3");
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            }
        }, "producer");

        Thread consumer = new Thread(() -> {
            try {
                for (int index = 0; index < 3; index++) {
                    System.out.println("   consumer received: " + box.take());
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            }
        }, "consumer");

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        System.out.println("   producer waited while the box was full; notifyAll woke the consumer");
    }

    private static final class CounterThread extends Thread {

        private CounterThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            System.out.println("   Thread subclass is running on " + getName());
        }
    }

    private static final class SynchronizedCounter {

        private int value;

        synchronized void increment() {
            value++;
        }

        synchronized int get() {
            return value;
        }
    }

    /**
     * Bounded mailbox. Synchronized methods guard the shared list; wait() releases
     * the monitor until another thread calls notifyAll().
     */
    private static final class MessageBox {

        private final int capacity;
        private final LinkedList<String> messages = new LinkedList<>();

        private MessageBox(int capacity) {
            this.capacity = capacity;
        }

        synchronized void put(String message) throws InterruptedException {
            while (messages.size() == capacity) {
                wait();
            }
            messages.add(message);
            notifyAll();
        }

        synchronized String take() throws InterruptedException {
            while (messages.isEmpty()) {
                wait();
            }
            String message = messages.removeFirst();
            notifyAll();
            return message;
        }
    }
}
