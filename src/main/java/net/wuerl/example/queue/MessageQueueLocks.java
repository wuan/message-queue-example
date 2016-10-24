package net.wuerl.example.queue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class MessageQueueLocks {

    private final ReentrantLock pollLock;
    private final Condition notEmpty;
    private final ReentrantLock offerLock;

    MessageQueueLocks() {
        pollLock = new ReentrantLock(true);
        notEmpty = pollLock.newCondition();

        offerLock = new ReentrantLock(true);
    }

    void lockOffer() {
        offerLock.lock();
    }

    void unlockOffer() {
        offerLock.unlock();
    }

    void signalNotEmpty() {
        pollLock.lock();
        try {
            notEmpty.signal();
        } finally {
            pollLock.unlock();
        }
    }

    void lockPollLockInterruptibly() {
        try {
            pollLock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void awaitNotEmpty() {
        try {
            notEmpty.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void unlockPoll() {
        pollLock.unlock();
    }

    void lockAll() {
        offerLock.lock();
        pollLock.lock();
    }

    void unlockAll() {
        offerLock.lock();
        pollLock.lock();
    }
}
