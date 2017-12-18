package com.sonar.trading.queue;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NonPersistentQueue<M> implements Queue<M> {

    private List<Subscription<M>> subscriptions = new ArrayList<>();

    @Override
    public void publish(M message) {
        for (Subscription<M> subscription: subscriptions) {
            subscription.onMessage(message);
        }
    }

    @Override
    public void subscribe(String subscriberId, Subscriber<M> subscriber) {
        subscriptions.add(new Subscription<M>(subscriber));
    }

    @Value
    private static class Subscription<M> {
        private final Subscriber<M> subscriber;
        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        public void onMessage(M message) {
            executor.submit(() -> {
                subscriber.onMessage(message);
            });
        }
    }
}
