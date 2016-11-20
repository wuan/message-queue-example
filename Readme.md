[![Build Status](https://travis-ci.org/wuan/message-queue-example.svg?branch=master)](https://travis-ci.org/wuan/message-queue-example)
[![Coverage Status](https://coveralls.io/repos/github/wuan/message-queue-example/badge.svg?branch=master)](https://coveralls.io/github/wuan/message-queue-example?branch=master)

# message-queue-example

Simple example for a blocking message queue

Overview: https://wuan.github.io/message-queue-example

## Usage

### queue.offer(message)

add element to the queue

```
    Message message = ImmutableMessage.builder()
                .what(1)
                .arg1(2)
                .arg2(3)
                .obj(payload)
                .build();

    messageQueue.offer(message);
```

Adding an element to the queue is independent of the size of the queue: O(1)

### queue.peek()

preview of first element of queue

```
    Optional<Message> message = messageQueue.peek();
```

The result is empty, if the queue is empty.

### queue.poll()

get element from the queue

```
    Message message = messageQueue.poll();
```

This operation blocks if the queue is empty.

Removing an element from the queue is independent of the size of the queue: O(1)

### queue.deleteSpecific(what)

remove all elements from queue with have the given `what` value

```
    messageQueue.deleteSpecific(5);
```

This operation requires to iterate over all elements of the queue: O(n)

## Build

Run build with

```
> ./gradlew build
```
