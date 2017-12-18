package com.sonar.trading.trading;

import com.sonar.trading.messages.ExchangeMessage;

/**
 * A trading strategy looks at incoming messages and makes a decision.
 * The strategy implementation must be stateless and if a state is needed, it must be explicitly returned as part of
 * decision.
 * @param <S> Type of strategy state.
 */
public interface TradingStrategy<S extends TradingStrategyState> {

    /**
     * @return ID for the strategy.
     */
    String getId();

    /**
     * @return State class used for serialization/deserialization.
     */
    Class<S> getStateClass();

    /**
     * Creates a new blank/initial state for the strategy.
     * @return created state.
     */
    S createState();

    /**
     * Checks if the given message can be accepted by strategy as part of makeDecision call.
     * @param message Message that needs to be checked.
     * @return TRUE if message is accepted as part of makeDecision call.
     */
    boolean acceptsMessage(ExchangeMessage message);

    /**
     * The main logic for the strategy. The logic takes in current state & new message and returns the decision containing
     * updated state and outcome.
     * @param currentState Current state for the strategy.
     * @param message Message on which decision needs to be made.
     * @return Decision (new state + outcome)
     */
    TradingDecision<S> makeDecision(S currentState, ExchangeMessage message);
}
