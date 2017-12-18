package com.sonar.trading.trading;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of TradingBotStateManager that maintains state in memory.
 * The states will be lost between restart of app.
 */
public class NonPersistentTradingBotStateManager implements TradingBotStateManager {

    private final Map<String, TradingStrategyState> states = new ConcurrentHashMap<>();

    @Override
    public <S extends TradingStrategyState> S getState(String key, Class<S> stateClass) {
        return (S) states.get(key);
    }

    @Override
    public <S extends TradingStrategyState> void createState(String key, S state) {
        states.put(key, state);
    }

    @Override
    public <S extends TradingStrategyState> boolean checkAndSetState(String key, S state, long expectedOldStateVersion) {
        S oldState = (S) states.get(key);
        if (state.getVersion() != oldState.getVersion() && oldState.getVersion() == expectedOldStateVersion) {
            states.put(key, state);
            return true;
        }
        return false;
    }
}
