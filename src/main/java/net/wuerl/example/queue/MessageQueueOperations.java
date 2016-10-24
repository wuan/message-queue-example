package net.wuerl.example.queue;

import java.util.Optional;
import java.util.function.Supplier;

public class MessageQueueOperations {
    public static final Supplier<IllegalStateException> SHOULD_NOT_HAPPEN_EXCEPTION_SUPPLIER = () -> new IllegalStateException("should not happen");

    private volatile ModifiableNode head;

    private volatile ModifiableNode tail;

    public MessageQueueOperations() {
        head = tail = ModifiableNode.create();
    }

    void enqueue(Message message) {
        final ModifiableNode node = ModifiableNode.create().setMessage(message);

        tail.setNext(node);
        tail = node;
    }

    Message dequeue() {
        ModifiableNode node = head;

        head = node.next().orElseThrow(SHOULD_NOT_HAPPEN_EXCEPTION_SUPPLIER);

        Optional<Message> message = head.message();
        head.setMessage(Optional.empty());
        node.clear();

        return message.orElseThrow(SHOULD_NOT_HAPPEN_EXCEPTION_SUPPLIER);
    }

    void deleteSpecific(int whatToDelete) {
        Optional<ModifiableNode> node = Optional.of(head);

        while (node.isPresent()) {
            removeNextNodeIfApplicable(whatToDelete, node.get());

            node = node.get().next();
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
