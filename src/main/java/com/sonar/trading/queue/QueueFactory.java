package com.sonar.trading.queue;

import com.sonar.trading.messages.Exchange;
import com.sonar.trading.messages.ExchangeMessageType;

public interface QueueFactory<M> {

    <N extends M> Queue<N> createQueue(Exchange exchange, ExchangeMessageType exchangeMessageType, String queueId, Class<N> queueMessageClass);

}
