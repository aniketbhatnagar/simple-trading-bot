package com.sonar.trading.trading;

import com.sonar.trading.currency.Currency;
import com.sonar.trading.messages.TradeMessage;
import com.sonar.trading.messages.TradingActivity;
import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TicksBasedTradingStrategyTest {

    @Test
    public void whenNewStateIsCreated_thenAllStateFieldsAreProperlyInitialized() {
        TicksBasedTradingStrategy strategy = new TicksBasedTradingStrategy(3, 3);
        TicksBasedTradingStrategyState state = strategy.createState();
        assertThat(state.getConsecutiveDownticks()).isZero();
        assertThat(state.getConsecutiveUpticks()).isZero();
        assertThat(state.getVersion()).isZero();
        assertThat(state.getLastPrice()).isEmpty();
    }

    @Test
    public void whenMConsecutiveUpTicksArePassed_thenSellOutcomeIsReturned() {
        TicksBasedTradingStrategy strategy = new TicksBasedTradingStrategy(3, 1);
        TicksBasedTradingStrategyState state = strategy.createState();
        for (int i = 1; i <= 3; i++) {
            TradingDecision<TicksBasedTradingStrategyState> decision = strategy.makeDecision(state, createMessage(i));
            assertThat(decision.getOutcome()).isNull();
            state = decision.getState();
            assertThatStateHasUpticks(state, i - 1, 0);
            assertThat(state.getLastPrice()).hasValue((long) i);
        }
        TradingDecision<TicksBasedTradingStrategyState> decision = strategy.makeDecision(state, createMessage(4));
        assertThat(decision.getOutcome()).isNotNull();
        assertThat(decision.getOutcome().getTradingActivity()).isEqualTo(TradingActivity.SELL);
        assertThatStateHasUpticks(decision.getState(), 0, 0); // tick counts are reset after signal.
    }

    @Test
    public void whenNConsecutiveDownTicksArePassed_thenBuyOutcomeIsReturned() {
        TicksBasedTradingStrategy strategy = new TicksBasedTradingStrategy(1, 3);
        TicksBasedTradingStrategyState state = strategy.createState();
        for (int i = 1; i <= 3; i++) {
            long priceInSubUnits = 5 - i;
            TradingDecision<TicksBasedTradingStrategyState> decision = strategy.makeDecision(state, createMessage(priceInSubUnits));
            assertThat(decision.getOutcome()).isNull();
            state = decision.getState();
            assertThatStateHasUpticks(state, 0, i - 1);
            assertThat(state.getLastPrice()).hasValue(priceInSubUnits);
        }
        TradingDecision<TicksBasedTradingStrategyState> decision = strategy.makeDecision(state, createMessage(1));
        assertThat(decision.getOutcome()).isNotNull();
        assertThat(decision.getOutcome().getTradingActivity()).isEqualTo(TradingActivity.BUY);
        assertThatStateHasUpticks(decision.getState(), 0, 0); // tick counts are reset after signal.
    }

    @Test
    public void whenNConsecutiveNoTicksArePassed_thenNoOutcomeIsReturned() {
        TicksBasedTradingStrategy strategy = new TicksBasedTradingStrategy(3, 3);
        TicksBasedTradingStrategyState state = strategy.createState();
        long priceInSubUnits = 10;
        for (int i = 1; i <= 100; i++) {
            TradingDecision<TicksBasedTradingStrategyState> decision = strategy.makeDecision(state, createMessage(priceInSubUnits));
            assertThat(decision.getOutcome()).isNull();
            state = decision.getState();
            assertThatStateHasUpticks(state, 0, 0);
            assertThat(state.getLastPrice()).hasValue(priceInSubUnits);
        }
    }

    @Test
    public void whenNoTicksHappenBetweenUpTicks_thenSellOrderIsReturned() {
        TicksBasedTradingStrategy strategy = new TicksBasedTradingStrategy(3, 3);
        TicksBasedTradingStrategyState state = strategy.createState();
        state = upTick(state, strategy, null);
        state = noTick(state, strategy, null);
        state = upTick(state, strategy, null);
        state = noTick(state, strategy, null);
        state = upTick(state, strategy, null);
        state = noTick(state, strategy, null);
        upTick(state, strategy, TradingActivity.SELL);
    }

    @Test
    public void whenDownTicksHappenBetweenUpTicks_thenSellOrderIsReturnedOnlyForConsecutiveUpTicks() {
        TicksBasedTradingStrategy strategy = new TicksBasedTradingStrategy(3, 3);
        TicksBasedTradingStrategyState state = strategy.createState();
        state = upTick(state, strategy, null);
        state = downTick(state, strategy, null);
        state = upTick(state, strategy, null);
        state = downTick(state, strategy, null);
        state = upTick(state, strategy, null);
        state = upTick(state, strategy, null);
        upTick(state, strategy, TradingActivity.SELL);
    }

    @Test
    public void whenNoTicksHappenBetweenDownTicks_thenBuyOrderIsReturned() {
        TicksBasedTradingStrategy strategy = new TicksBasedTradingStrategy(3, 3);
        TicksBasedTradingStrategyState state = strategy.createState();
        state = downTick(state, strategy, null);
        state = noTick(state, strategy, null);
        state = downTick(state, strategy, null);
        state = noTick(state, strategy, null);
        state = downTick(state, strategy, null);
        state = noTick(state, strategy, null);
        downTick(state, strategy, TradingActivity.BUY);
    }

    @Test
    public void whenUpTicksHappenBetweenDownTicks_thenBuyOrderIsReturnedOnlyForConsecutiveDownTicks() {
        TicksBasedTradingStrategy strategy = new TicksBasedTradingStrategy(3, 3);
        TicksBasedTradingStrategyState state = strategy.createState();
        state = downTick(state, strategy, null);
        state = upTick(state, strategy, null);
        state = downTick(state, strategy, null);
        state = upTick(state, strategy, null);
        state = downTick(state, strategy, null);
        state = downTick(state, strategy, null);
        downTick(state, strategy, TradingActivity.BUY);
    }

    @Test
    public void whenAlternateUpTickAndDownTickHappens_thenNoOutcomeIsReturned() {
        TicksBasedTradingStrategy strategy = new TicksBasedTradingStrategy(3, 3);
        TicksBasedTradingStrategyState state = strategy.createState();
        for (int i = 1; i <= 100; i ++) {
            if (i % 2 == 0) {
                state = upTick(state, strategy, null);
            } else {
                state = downTick(state, strategy, null);
            }
        }
    }

    private TicksBasedTradingStrategyState upTick(TicksBasedTradingStrategyState state, TicksBasedTradingStrategy strategy, TradingActivity expectedTradingActivity) {
        long priceInSubUnits = state.getLastPrice().orElse(10000L) + 1;
        return tick(state, strategy, expectedTradingActivity, priceInSubUnits);
    }

    private TicksBasedTradingStrategyState downTick(TicksBasedTradingStrategyState state, TicksBasedTradingStrategy strategy, TradingActivity expectedTradingActivity) {
        long priceInSubUnits = state.getLastPrice().orElse(10000L) - 1;
        return tick(state, strategy, expectedTradingActivity, priceInSubUnits);
    }

    private TicksBasedTradingStrategyState noTick(TicksBasedTradingStrategyState state, TicksBasedTradingStrategy strategy, TradingActivity expectedTradingActivity) {
        long priceInSubUnits = state.getLastPrice().orElse(10000L);
        return tick(state, strategy, expectedTradingActivity, priceInSubUnits);
    }

    private TicksBasedTradingStrategyState tick(TicksBasedTradingStrategyState state, TicksBasedTradingStrategy strategy, TradingActivity expectedTradingActivity, long priceInSubUnits) {
        TradingDecision<TicksBasedTradingStrategyState> decision = strategy.makeDecision(state, createMessage(priceInSubUnits));
        if (expectedTradingActivity == null) {
            assertThat(decision.getOutcome()).isNull();
        } else {
            assertThat(decision.getOutcome().getTradingActivity()).isEqualTo(expectedTradingActivity);
        }
        return decision.getState();
    }

    private void assertThatStateHasUpticks(TicksBasedTradingStrategyState state, int upTicks, int downTicks) {
        assertThat(state.getConsecutiveUpticks()).isEqualTo(upTicks);
        assertThat(state.getConsecutiveDownticks()).isEqualTo(downTicks);
    }

    private TradeMessage createMessage(long priceInSubUnits) {
        return TradeMessage.builder()
                .id(UUID.randomUUID().toString())
                .sourceCurrency(Currency.BTC)
                .amountInSubunits(1)
                .destinationCurrency(Currency.MXN)
                .priceInSubunits(priceInSubUnits)
                .tradingActivity(TradingActivity.BUY)
                .build();
    }

}
