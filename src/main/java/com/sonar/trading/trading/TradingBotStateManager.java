package com.sonar.trading.trading;

/**
 * Maintains state for trading bot and trading bot strategies so that they are potentially available
 * on restart/after failures.
 */
public interface TradingBotStateManager {

    /**
     * Gets an already persisted state by key.
     * @param key Key for the state.
     * @param stateClass The class for the state. Used for deserialization purposes.
     * @param <S> The type of state.
     * @return Deserialized state object.
     */
    <S extends TradingStrategyState> S getState(String key, Class<S> stateClass);

    /**
     * Creates a new state by key.
     * @param key Key for the state.
     * @param state State to set.
     * @param <S> The type of state.
     */
    <S extends TradingStrategyState> void createState(String key, S state);

    /**
     * Checks if the state is in given version and if so, updates the state with new state by key.
     * In other words, this is CAS operation on the state.
     *
     * @param key Key for the state.
     * @param state State to set.
     * @param expectedOldStateVersion The expected version in which current state is in. If this does not match, this operation will fail.
     * @param <S> The type of state.
     * @return
     */
    <S extends TradingStrategyState> boolean checkAndSetState(String key, S state, long expectedOldStateVersion);
}
