package com.sonar.trading.trading;

import com.sonar.trading.messages.ExchangeMessage;
import com.sonar.trading.queue.Subscriber;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Trading bot is a subscriber for all exchange feed messages and for each message, executes trading strategies
 * and takes action on the decision returned by the strategies.
 * @param <M> Type of message subscribed by the bot.
 */
@RequiredArgsConstructor
public class TradingBot<M extends ExchangeMessage> implements Subscriber<M> {
    private static final Logger LOGGER = LoggerFactory.getLogger("trading");

    private static final String STRATEGY_STATE_KEY_NAMESPACE = "trading.strategy.";

    private final TradingBotStateManager tradingBotStateManager;
    private final TradingOperationsListener tradingOperationsListener;
    private final List<TradingStrategy<? extends TradingStrategyState>> tradingStrategies;

    @Override
    public void onMessage(M message) {
        for (TradingStrategy<? extends TradingStrategyState> tradingStrategy: tradingStrategies) {
            makeDecision(message, tradingStrategy);
        }
    }

    private <T extends TradingStrategyState> void makeDecision(M message, TradingStrategy<T> tradingStrategy) {
        if (tradingStrategy.acceptsMessage(message)) {
            String strategyStateKey = STRATEGY_STATE_KEY_NAMESPACE + tradingStrategy.getId();

            T state = tradingBotStateManager.getState(strategyStateKey, tradingStrategy.getStateClass());
            if (state == null) {
                state = tradingStrategy.createState();
                tradingBotStateManager.createState(strategyStateKey, state);
            }

            TradingDecision<T> decision = tradingStrategy.makeDecision(state, message);
            LOGGER.debug("Outcome for Strategy {} is {} for message {} in old state {} and new state {}",
                    new Object[] {tradingStrategy.getId(), decision.getOutcome(), message, state, decision.getState()});

            TradingStrategyState newState = decision.getState();
            if (newState.getVersion() != state.getVersion()) {
                if (updateState(strategyStateKey, state, newState)) {
                    executeDecision(tradingStrategy, decision);
                } else {
                    LOGGER.warn("State could not updated. Old state was {}. Decision {} will be ignored.", state, decision);
                }
            }
        }
    }

    private <T extends TradingStrategyState> void executeDecision(TradingStrategy<T> tradingStrategy,
                                 TradingDecision<T> decision) {
        if (decision.getOutcome() != null) {
            tradingOperationsListener.onSignal(tradingStrategy.getId(), decision.getState(), decision.getOutcome());
        }
    }

    private boolean updateState(String strategyStateKey, TradingStrategyState oldState, TradingStrategyState newState) {
        return tradingBotStateManager.checkAndSetState(strategyStateKey, newState, oldState.getVersion());
    }
}
