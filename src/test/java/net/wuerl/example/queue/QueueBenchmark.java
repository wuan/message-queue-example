package net.wuerl.example.queue;

import org.openjdk.jmh.annotations.Benchmark;

public class QueueBenchmark {

    public static final int NUMBER_OF_ELEMENTS = 1000;

    @Benchmark
    public static void baselineBenchmark() {
        MessageQueue messageQueue = new MessageQueue();
        Object object = new Object();

        for (int i = 0; i < 1000; i++) {
            Message message = ImmutableMessage.builder().what(i).arg1(0).arg2(0).obj(object).build();
            assert i == message.what();
        }
    }

    @Benchmark
    public static void offerBenchmark() {
        MessageQueue messageQueue = new MessageQueue();
        Object object = new Object();

        for (int i = 0; i < 1000; i++) {
            Message message = ImmutableMessage.builder().what(i).arg1(0).arg2(0).obj(object).build();
            messageQueue.offer(message);
        }
    }

    @Benchmark
    public static void offerAndPollBenchmark() {
        MessageQueue messageQueue = new MessageQueue();
        Object object = new Object();

        for (int i = 0; i < NUMBER_OF_ELEMENTS; i++) {
            Message message = ImmutableMessage.builder().what(i).arg1(0).arg2(0).obj(object).build();
            messageQueue.offer(message);
        }

        for (int i = 0; i < NUMBER_OF_ELEMENTS; i++) {
            Message message = messageQueue.poll();
            assert i == message.what();
        }
    }

    @Benchmark
    public static void offerPeekAndPollBenchmark() {
        MessageQueue messageQueue = new MessageQueue();
        Object object = new Object();

        for (int i = 0; i < NUMBER_OF_ELEMENTS; i++) {
            Message message = ImmutableMessage.builder().what(i).arg1(0).arg2(0).obj(object).build();
            messageQueue.offer(message);
        }

        for (int i = 0; i < NUMBER_OF_ELEMENTS; i++) {
            Message peekMessage = messageQueue.peek().get();
            Message message = messageQueue.poll();
            assert i == message.what();
            assert i == peekMessage.what();
        }
    }
}
