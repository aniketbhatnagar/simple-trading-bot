package com.sonar.trading.trading;

import com.sonar.trading.messages.SignalMessage;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * A trading decision is a combination of outcome (buy, sell, etc) and the new strategy's state as a result of a
 * trading message.
 * @param <S> The type of strategy state.
 */
@Value
@RequiredArgsConstructor
public class TradingDecision<S extends TradingStrategyState> {
    private final S state;
    private final SignalMessage outcome;
}
