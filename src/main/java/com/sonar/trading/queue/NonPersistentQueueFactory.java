package com.sonar.trading.queue;

public class NonPersistentQueueFactory<M> extends AbstractCachingQueueFactory<M> {

    @Override
    protected <N extends M> Queue<N> createQueue() {
        return new NonPersistentQueue<>();
    }
}
