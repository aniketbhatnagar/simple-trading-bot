package com.sonar.trading.trading;

import com.sonar.trading.messages.SignalMessage;

/**
 * Listener for all trading bot operation events.
 */
public interface TradingOperationsListener {

    /**
     * Called when trading strategy signals that a buy/sell order can be placed.
     * @param strategyId ID of the strategy.
     * @param state The strategy's state.
     * @param message Message containing details of the trade order to execute.
     */
    void onSignal(String strategyId, TradingStrategyState state, SignalMessage message);
}
