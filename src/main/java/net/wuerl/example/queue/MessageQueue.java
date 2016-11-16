package net.wuerl.example.queue;


import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageQueue {

    private final MessageQueueOperations queueOperations;

    private final MessageQueueLocks locks;

    private final AtomicInteger count = new AtomicInteger();

    public MessageQueue() {
        queueOperations = new MessageQueueOperations();
        locks = new MessageQueueLocks();
    }

    boolean isEmpty() {
        return count.get() == 0;
    }

    /**
     * adds a new message to the end of the queue
     *
     * @param message message to be added
     */
    public void offer(Message message) {

        locks.lockOffer();
        try {
            queueOperations.enqueue(message);
        } finally {
            locks.unlockOffer();
        }

        if (count.incrementAndGet() == 1) {
            locks.signalNotEmpty();
        }
    }

    /**
     * reads message from queue
     * <p>
     * if the queue is empty this operation blocks until a message is added
     *
     * @return first element from queue
     */
    public Message poll() {
        final Message message;

        locks.lockPollLockInterruptibly();

        try {
            while (count.get() == 0) {
                locks.awaitNotEmpty();
            }
            message = queueOperations.dequeue();
        } finally {
            locks.unlockPoll();
        }

        count.decrementAndGet();

        return message;
    }

    /**
     * previews next available message from queue
     *
     * @return first element from queue or empty if there is nothing
     */
    public Optional<Message> peek() {
        return queueOperations.peek();
    }

    /**
     * delete all messages in the queue which have the given value of {@link Message#what()}
     *
     * @param whatToDelete value of what for which messages should be deleted
     */
    public void deleteSpecific(int whatToDelete) {
        locks.lockAll();
        try {
            queueOperations.deleteSpecific(whatToDelete);
        } finally {
            locks.unlockAll();
        }
    }

}
