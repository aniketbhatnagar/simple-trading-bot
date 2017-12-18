package com.sonar.trading.queue;

import java.util.ArrayList;
import java.util.List;

public class TestSynchronousQueue<M> implements Queue<M> {

    private List<Subscriber<M>> subscriptions = new ArrayList<>();

    @Override
    public void publish(M message) {
        for (Subscriber<M> subscription: subscriptions) {
            subscription.onMessage(message);
        }
    }

    @Override
    public void subscribe(String subscriberId, Subscriber<M> subscriber) {
        subscriptions.add(subscriber);
    }
}
