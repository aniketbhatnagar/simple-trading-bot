package com.sonar.trading.trading;

import com.sonar.trading.config.AppConfig;
import com.sonar.trading.config.TradingConfig;
import com.sonar.trading.messages.Exchange;
import com.sonar.trading.messages.ExchangeMessage;
import com.sonar.trading.messages.ExchangeMessageType;
import com.sonar.trading.messages.SignalMessage;
import com.sonar.trading.queue.Queue;
import com.sonar.trading.queue.QueueFactory;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;

public class TradingModule {

    @Getter
    private Queue<SignalMessage> signalMessageQueue;

    public void init(AppConfig appConfig, Collection<Queue<? extends ExchangeMessage>> exchangedFeedQueues, QueueFactory<ExchangeMessage> queueFactory) {
        TradingConfig tradingConfig = appConfig.getTrading();
        TradingBotStateManager stateManager = new NonPersistentTradingBotStateManager();
        TradingStrategy<? extends TradingStrategyState> strategy = new TicksBasedTradingStrategy(
                tradingConfig.getNumUpTicksToPlaceSellOrder(),
                tradingConfig.getNumDownTicksToPlaceBuyOrder());
        signalMessageQueue = queueFactory.createQueue(Exchange.BITSO, ExchangeMessageType.TRADE, "signal-" + strategy.getId(), SignalMessage.class);
        TradingOperationsListener tradingOperationsListener = new QueuingTradingOperationsListener(signalMessageQueue);
        TradingBot tradingBot = new TradingBot(stateManager, tradingOperationsListener, Collections.singletonList(strategy));
        for (Queue<? extends ExchangeMessage> queue: exchangedFeedQueues) {
            queue.subscribe("tradingBot", tradingBot);
        }
    }
}
