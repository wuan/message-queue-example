package net.wuerl.example.queue;


import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class MessageQueue {

    private final ReentrantLock pollLock;

    private final Condition notEmpty;

    private final ReentrantLock offerLock;

    private final AtomicInteger count = new AtomicInteger();

    private volatile ModifiableNode head;

    private volatile ModifiableNode tail;

    public MessageQueue() {
        head = tail = ModifiableNode.create();

        pollLock = new ReentrantLock(true);
        notEmpty = pollLock.newCondition();

        offerLock = new ReentrantLock(true);
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

        offerLock.lock();
        try {
            enqueue(message);
        } finally {
            offerLock.unlock();
        }

        if (count.incrementAndGet() == 1) {
            signalNotEmpty();
        }
    }

    private void enqueue(Message message) {
        final ModifiableNode node = ModifiableNode.create().setMessage(message);

        tail.setNext(node);
        tail = node;
    }

    private void signalNotEmpty() {
        pollLock.lock();
        try {
            notEmpty.signal();
        } finally {
            pollLock.unlock();
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

        lockPollLockInterruptibly();

        try {
            while (count.get() == 0) {
                awaitNotEmpty();
            }
            message = dequeue();
        } finally {
            pollLock.unlock();
        }

        count.decrementAndGet();

        return message;
    }

    private void lockPollLockInterruptibly() {
        try {
            pollLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void awaitNotEmpty() {
        try {
            notEmpty.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Message dequeue() {
        ModifiableNode node = head;
        Supplier<IllegalStateException> shouldNotHappen = () -> new IllegalStateException("should not happen");

        head = node.next().orElseThrow(shouldNotHappen);

        Optional<Message> message = head.message();
        head.setMessage(Optional.empty());
        node.clear();

        return message.orElseThrow(shouldNotHappen);
    }

    /**
     * delete all messages in the queue which have the given value of {@link Message#what()}
     *
     * @param whatToDelete value of what for which messages should be deleted
     */
    public void deleteSpecific(int whatToDelete) {
        lockQueue();
        try {
            Optional<ModifiableNode> node = Optional.of(head);

            while (node.isPresent()) {
                removeNextNodeIfApplicable(whatToDelete, node.get());

                node = node.get().next();
            }
        } finally {
            unlockQueue();
        }
    }

    private void removeNextNodeIfApplicable(int whatToDelete, final ModifiableNode node) {
        node.next().ifPresent(
            nextNode -> nextNode.message().ifPresent(
                nextMessage -> {
                    if (nextMessage.what() == whatToDelete) {
                        skipNextNode(node, nextNode);
                    }
                }
            )
        );
    }

    private void skipNextNode(ModifiableNode node, ModifiableNode nextNode) {
        node.setNext(nextNode.next());
        nextNode.clear();
    }

    private void lockQueue() {
        offerLock.lock();
        pollLock.lock();
    }

    private void unlockQueue() {
        offerLock.lock();
        pollLock.lock();
    }
}
