package com.sonar.trading.trading;

import com.sonar.trading.messages.ExchangeMessage;
import com.sonar.trading.messages.ExchangeMessageType;
import com.sonar.trading.messages.SignalMessage;
import com.sonar.trading.messages.TradeMessage;
import com.sonar.trading.messages.TradingActivity;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

/**
 * A trading strategy implementation that counts the numUpTicksToPlaceSellOrder consecutive upticks
 * and numDownTicksToPlaceBuyOrder consecutive downticks.
 * A trade that executes at a price that is the same as the price of the trade that executed immediately preceding it is
 * known as a “zero tick”. An uptick is when a trade executes at a higher price than the most recent non-zero-tick trade
 * before it. A downtick is when a trade executes at a lower price than the most recent non-zero-tick trade before it.
 * After numUpTicksToPlaceSellOrder consecutive upticks, the algorithm will signal a buy at the price of the most recent uptick.
 * After N consecutive downticks, the algorithm will signal a sell at the price of the most recent downtick.
 */
@RequiredArgsConstructor
public class TicksBasedTradingStrategy implements TradingStrategy<TicksBasedTradingStrategyState> {

    public static final String STRATEGY_ID = "TicksBasedTradingStrategy";

    private final int numUpTicksToPlaceSellOrder;
    private final int numDownTicksToPlaceBuyOrder;

    @Override
    public String getId() {
        return STRATEGY_ID;
    }

    @Override
    public Class<TicksBasedTradingStrategyState> getStateClass() {
        return TicksBasedTradingStrategyState.class;
    }

    @Override
    public TicksBasedTradingStrategyState createState() {
        return new TicksBasedTradingStrategyState(0L, 0L, Optional.empty(), 0L);
    }

    @Override
    public boolean acceptsMessage(ExchangeMessage message) {
        return message.getMessageType() == ExchangeMessageType.TRADE && message instanceof TradeMessage;
    }

    @Override
    public TradingDecision<TicksBasedTradingStrategyState> makeDecision(TicksBasedTradingStrategyState currentState, ExchangeMessage message) {
        TradeMessage tradeMessage = (TradeMessage) message;
        Optional<Long> lastPriceOpt = currentState.getLastPrice();
        long currentPrice = tradeMessage.getPriceInSubunits();
        if (!lastPriceOpt.isPresent()) {
            return noOutcome(currentState.withUpdatedPrice(currentPrice));
        }
        long lastPrice = lastPriceOpt.get();
        TicksBasedTradingStrategyState newState;
        if (lastPrice < currentPrice) {
            newState = currentState.withUpTick(currentPrice);
        } else if (lastPrice > currentPrice) {
            newState = currentState.withDownTick(currentPrice);
        } else {
            // no tick. No need to udpdate state.
            newState = currentState;
        }
        return computeOutcome(tradeMessage, newState);
    }

    private TradingDecision<TicksBasedTradingStrategyState> computeOutcome(TradeMessage tradeMessage, TicksBasedTradingStrategyState state) {
        if (state.getConsecutiveUpticks() >= numUpTicksToPlaceSellOrder) {
            return sell(state, tradeMessage);
        } if (state.getConsecutiveDownticks() >= numDownTicksToPlaceBuyOrder) {
            return buy(state, tradeMessage);
        } else {
            return noOutcome(state);
        }
    }

    private TradingDecision<TicksBasedTradingStrategyState> buy(TicksBasedTradingStrategyState state, TradeMessage tradeMessage) {
        SignalMessage signalMessage = new SignalMessage(UUID.randomUUID().toString(), tradeMessage.getTimestamp(), tradeMessage.getExchange(),
                tradeMessage.getSourceCurrency(), tradeMessage.getDestinationCurrency(), tradeMessage.getPriceInSubunits(), TradingActivity.BUY);
        return new TradingDecision<>(state.withTicksReset(), signalMessage);
    }


    private TradingDecision<TicksBasedTradingStrategyState> sell(TicksBasedTradingStrategyState state, TradeMessage tradeMessage) {
        SignalMessage signalMessage = new SignalMessage(UUID.randomUUID().toString(), tradeMessage.getTimestamp(), tradeMessage.getExchange(),
                tradeMessage.getSourceCurrency(), tradeMessage.getDestinationCurrency(), tradeMessage.getPriceInSubunits(), TradingActivity.SELL);
        return new TradingDecision<>(state.withTicksReset(), signalMessage);
    }

    private TradingDecision<TicksBasedTradingStrategyState> noOutcome(TicksBasedTradingStrategyState state) {
        return new TradingDecision<>(state, null);
    }
}
