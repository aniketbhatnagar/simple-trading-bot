package com.sonar.trading.queue;

import com.sonar.trading.messages.Exchange;
import com.sonar.trading.messages.ExchangeMessageType;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCachingQueueFactory<M> implements QueueFactory<M> {

    private final Map<String, Queue<? extends M>> queues = new HashMap<>();

    @Override
    public <N extends M> Queue<N> createQueue(Exchange exchange, ExchangeMessageType exchangeMessageType, String queueId, Class<N> queueMessageClass) {
        String fullyQualifiedQueueId = "exchangeMessages." + exchange + "." + exchangeMessageType + "." + queueId;
        return createQueue(fullyQualifiedQueueId);
    }

    private synchronized <N extends M> Queue<N> createQueue(String fullyQualifiedQueueId) {
        Queue<N> queue = (Queue<N>) queues.get(fullyQualifiedQueueId);
        if (queue == null) {
            queue = createQueue();
            queues.put(fullyQualifiedQueueId, queue);
        }
        return queue;
    }

    protected abstract <N extends M> Queue<N> createQueue();
}
