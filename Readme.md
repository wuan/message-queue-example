# message-queue-example

Simple example for a blocking message queue

## Usage

### queue.offer(message) &mdash; add element to the queue

```
    Message message = ImmutableMessage.builder()
                .what(1)
                .arg1(2)
                .arg2(3)
                .obj(payload)
                .build();

    messageQueue.offer(message);
```

### queue.poll() &mdash; get element from the queue

```
    Message message = messageQueue.poll();
```

This operation blocks if the queue is empty.

### queue.deleteSpecific(what) &mdash; remove all elements from queue with have the given `what` value

```
    messageQueue.deleteSpecific(5);
```

