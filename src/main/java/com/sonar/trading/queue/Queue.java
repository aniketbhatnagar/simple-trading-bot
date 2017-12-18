package com.sonar.trading.queue;

public interface Queue<M> {
    void publish(M message);

    void subscribe(String subscriberId, Subscriber<M> subscriber);
}
