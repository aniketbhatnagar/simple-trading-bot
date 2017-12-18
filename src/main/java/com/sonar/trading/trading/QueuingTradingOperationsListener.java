package com.sonar.trading.trading;

import com.sonar.trading.messages.SignalMessage;
import com.sonar.trading.queue.Queue;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of TradingOperationsListener that queues SignalMessages.
 */
@RequiredArgsConstructor
public class QueuingTradingOperationsListener implements TradingOperationsListener {
    private static final Logger LOGGER = LoggerFactory.getLogger("trading");

    private final Queue<SignalMessage> signalMessageQueue;

    @Override
    public void onSignal(String strategyId, TradingStrategyState state, SignalMessage message) {
        LOGGER.info("Strategy {} is signalling %s on message {} with state {}", new Object[] {strategyId, message.getTradingActivity(), message, state});
        signalMessageQueue.publish(message);
    }
}
