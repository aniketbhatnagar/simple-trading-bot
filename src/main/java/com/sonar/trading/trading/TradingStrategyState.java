package com.sonar.trading.trading;

/**
 * Represents a state of a trading strategy that can be persisted in an external store.
 */
public interface TradingStrategyState {

    /**
     * @return Version for the state. This version of the state must be monotonically increasing.
     */
    long getVersion();
}
