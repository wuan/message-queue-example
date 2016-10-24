package net.wuerl.example.queue;


import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class MessageQueue {

    private volatile ModifiableNode head;

    private volatile ModifiableNode tail;

    private final AtomicInteger count = new AtomicInteger();

    private final MessageQueueLocks locks;
    public static final Supplier<IllegalStateException> SHOULD_NOT_HAPPEN_EXCEPTION_SUPPLIER = () -> new IllegalStateException("should not happen");


    public MessageQueue() {
        head = tail = ModifiableNode.create();

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
            enqueue(message);
        } finally {
            locks.unlockOffer();
        }

        if (count.incrementAndGet() == 1) {
            locks.signalNotEmpty();
        }
    }

    private void enqueue(Message message) {
        final ModifiableNode node = ModifiableNode.create().setMessage(message);

        tail.setNext(node);
        tail = node;
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
            message = dequeue();
        } finally {
            locks.unlockPoll();
        }

        count.decrementAndGet();

        return message;
    }

    private Message dequeue() {
        ModifiableNode node = head;

        head = node.next().orElseThrow(SHOULD_NOT_HAPPEN_EXCEPTION_SUPPLIER);

        Optional<Message> message = head.message();
        head.setMessage(Optional.empty());
        node.clear();

        return message.orElseThrow(SHOULD_NOT_HAPPEN_EXCEPTION_SUPPLIER);
    }

    /**
     * delete all messages in the queue which have the given value of {@link Message#what()}
     *
     * @param whatToDelete value of what for which messages should be deleted
     */
    public void deleteSpecific(int whatToDelete) {
        locks.lockAll();
        try {
            Optional<ModifiableNode> node = Optional.of(head);

            while (node.isPresent()) {
                removeNextNodeIfApplicable(whatToDelete, node.get());

                node = node.get().next();
            }
        } finally {
            locks.unlockAll();
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

}
