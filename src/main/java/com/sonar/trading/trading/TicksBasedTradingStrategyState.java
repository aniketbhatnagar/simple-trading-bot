package com.sonar.trading.trading;

import lombok.Value;

import java.util.Optional;

@Value
public class TicksBasedTradingStrategyState implements TradingStrategyState {
    private final long consecutiveUpticks;
    private final long consecutiveDownticks;
    private final Optional<Long> lastPrice;
    private final long version;

    public TicksBasedTradingStrategyState withUpdatedPrice(long price) {
        return new TicksBasedTradingStrategyState(this.getConsecutiveUpticks(), this.getConsecutiveDownticks(), Optional.of(price), this.getVersion() + 1);
    }

    public TicksBasedTradingStrategyState withUpTick(long price) {
        return new TicksBasedTradingStrategyState(this.getConsecutiveUpticks() + 1, 0, Optional.of(price), this.getVersion() + 1);
    }

    public TicksBasedTradingStrategyState withDownTick(long price) {
        return new TicksBasedTradingStrategyState(0, this.getConsecutiveDownticks() + 1, Optional.of(price), this.getVersion() + 1);
    }

    public TicksBasedTradingStrategyState withTicksReset() {
        return new TicksBasedTradingStrategyState(0, 0, this.lastPrice, this.getVersion() + 1);
    }
}
