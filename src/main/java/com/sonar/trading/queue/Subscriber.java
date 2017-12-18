package com.sonar.trading.queue;

@FunctionalInterface
public interface Subscriber<M> {
    void onMessage(M message);
}
