package net.wuerl.example.queue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@RunWith(MockitoJUnitRunner.class)
public class MessageQueueTest {

    private MessageQueue messageQueue;

    @Before
    public void setUp() {
        messageQueue = new MessageQueue();
    }

    @Test
    public void createdQueueShouldBeEmpty() throws Exception {
        assertThat(messageQueue.isEmpty()).isTrue();
    }

    @Test
    public void canOfferMessages() throws Exception {
        messageQueue.offer(createMessage(1));

        assertThat(messageQueue.isEmpty()).isFalse();
    }

    @Test
    @Ignore
    public void emptyQueueReturnsEmptyMessage() throws Exception {
        messageQueue.poll();
    }

    @Test
    public void containedElementIsReturnedFromPoll() throws Exception {
        final Message message = createMessage(1);
        messageQueue.offer(message);

        assertThat(messageQueue.poll()).isEqualTo(message);
    }

    @Test
    public void messageIsEmptyAfterLastElementIsPolled() throws Exception {
        final Message message = createMessage(1);
        messageQueue.offer(message);
        messageQueue.poll();

        assertThat(messageQueue.isEmpty()).isTrue();
    }

    @Test
    public void firstInsertedMessageIsPolledFirst() throws Exception {
        final Message message1 = createMessage(1);
        messageQueue.offer(message1);

        final Message message2 = createMessage(1);
        messageQueue.offer(message2);

        assertThat(messageQueue.poll()).isEqualTo(message1);
    }

    @Test
    public void orderOfMessagesIsConserved() throws Exception {
        final Message message1 = createMessage(1);
        messageQueue.offer(message1);

        final Message message2 = createMessage(1);
        messageQueue.offer(message2);

        assertThat(messageQueue.poll()).isEqualTo(message1);
        assertThat(messageQueue.poll()).isEqualTo(message2);
    }

    @Test
    public void pollShouldBlockUntilMessageArrives() throws Exception {
        final Message message1 = createMessage(1);

        runInThread(() -> {
            sleep(500);
            messageQueue.offer(message1);
        });

        await().atLeast(400, TimeUnit.MILLISECONDS).until(() -> assertThat(messageQueue.poll()).isEqualTo(message1));
    }

    @Test
    public void simpleConcurrencyTest() {
        final int numberOfParallelThreads = 50;
        final int numberOfMessagesPerThread = 1000;

        IntStream.range(0, numberOfParallelThreads).forEach(index ->
                runInThread(() -> {
                    final Message message = createMessage(index);
                    IntStream.range(0, numberOfMessagesPerThread).forEach(ignore -> messageQueue.offer(message));
                }));

        final ResultRunner resultRunner = new ResultRunner(messageQueue, numberOfParallelThreads * numberOfMessagesPerThread);

        await().with().timeout(5, TimeUnit.SECONDS).until(resultRunner);

        final Map<Integer, Long> results = resultRunner.results;

        assertThat(results.keySet()).containsExactly(IntStream.range(0, numberOfParallelThreads).mapToObj(Integer::valueOf).toArray(Integer[]::new));
        assertThat(results.values()).containsExactly(IntStream.range(0, numberOfParallelThreads).mapToObj(ignore -> (long) numberOfMessagesPerThread).toArray(Long[]::new));
        assertThat(messageQueue.isEmpty()).isTrue();
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deleteSpecificShouldRemoveFirstElement() {
        messageQueue.offer(createMessage(1));
        messageQueue.offer(createMessage(2));
        messageQueue.offer(createMessage(3));

        messageQueue.deleteSpecific(1);

        assertThat(messageQueue.poll().what()).isEqualTo(2);
        assertThat(messageQueue.poll().what()).isEqualTo(3);
    }

    @Test
    public void deleteSpecificShouldRemoveLastElement() {
        messageQueue.offer(createMessage(1));
        messageQueue.offer(createMessage(2));
        messageQueue.offer(createMessage(3));

        messageQueue.deleteSpecific(3);

        assertThat(messageQueue.poll().what()).isEqualTo(1);
        assertThat(messageQueue.poll().what()).isEqualTo(2);
    }

    private void runInThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    private ImmutableMessage createMessage(int what) {
        return ImmutableMessage.builder()
                .what(what)
                .arg1(0)
                .arg2(0)
                .obj(new Object())
                .build();
    }

    static class ResultRunner implements Runnable {
        private final MessageQueue messageQueue;
        private final Integer totalNumber;
        Map<Integer, Long> results;

        public ResultRunner(MessageQueue messageQueue, Integer totalNumber) {
            this.messageQueue = messageQueue;
            this.totalNumber = totalNumber;
        }

        @Override
        public void run() {
            results = IntStream.range(0, totalNumber)
                    .mapToObj(ignore -> messageQueue.poll().what())
                    .collect(Collectors.groupingBy(what -> what, Collectors.counting()));
        }
    }
}