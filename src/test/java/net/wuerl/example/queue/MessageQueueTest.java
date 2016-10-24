package net.wuerl.example.queue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

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
}