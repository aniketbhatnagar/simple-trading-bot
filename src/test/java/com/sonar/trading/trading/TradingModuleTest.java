package com.sonar.trading.trading;

import com.sonar.trading.config.AppConfig;
import com.sonar.trading.config.TradingConfig;
import com.sonar.trading.currency.Currency;
import com.sonar.trading.messages.Exchange;
import com.sonar.trading.messages.ExchangeMessage;
import com.sonar.trading.messages.ExchangeMessageType;
import com.sonar.trading.messages.SignalMessage;
import com.sonar.trading.messages.TradeMessage;
import com.sonar.trading.messages.TradingActivity;
import com.sonar.trading.queue.Queue;
import com.sonar.trading.queue.QueueFactory;
import com.sonar.trading.queue.Subscriber;
import com.sonar.trading.queue.TestSynchronousQueueFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class TradingModuleTest {

    private QueueFactory<ExchangeMessage> queueFactory = new TestSynchronousQueueFactory<>();
    private Queue<TradeMessage> tradeMessageQueue = queueFactory.createQueue(Exchange.BITSO, ExchangeMessageType.TRADE, "testTradeQueue", TradeMessage.class);

    private TradingModule tradingModule = new TradingModule();
    private Queue<SignalMessage> signalQueue;
    private long currentPriceInSubunits = 10000;
    private List<SignalMessage> receivedSignals = new ArrayList<>();

    @Before
    public void setup() {
        AppConfig appConfig = new AppConfig();
        TradingConfig tradingConfig = new TradingConfig();
        tradingConfig.setNumDownTicksToPlaceBuyOrder(3);
        tradingConfig.setNumUpTicksToPlaceSellOrder(3);
        appConfig.setTrading(tradingConfig);
        tradingModule.init(appConfig, Collections.singletonList(tradeMessageQueue), queueFactory);
        signalQueue = tradingModule.getSignalMessageQueue();
        signalQueue.subscribe("testSubscriber", new Subscriber<SignalMessage>() {
            @Override
            public void onMessage(SignalMessage message) {
                receivedSignals.add(message);
            }
        });
    }

    @Test
    public void whenConsecutiveUpTicksAreFed_thenSellSignalIsReturnedFromSignalQueue() {
        upTick(null);
        upTick(null);
        upTick(null);
        upTick(TradingActivity.SELL);
    }

    @Test
    public void whenConsecutiveDownTicksAreFed_thenBuySignalIsReturnedFromSignalQueue() {
        downTick(null);
        downTick(null);
        downTick(null);
        downTick(TradingActivity.BUY);
    }

    @Test
    public void whenNoTicksAppearBetweenConsecutiveDownTicksAreFed_thenBuySignalIsReturnedFromSignalQueue() {
        downTick(null);
        noTick(null);
        downTick(null);
        noTick(null);
        downTick(null);
        downTick(TradingActivity.BUY);
    }

    private void upTick(TradingActivity tradingActivity) {
        currentPriceInSubunits = currentPriceInSubunits + 1;
        tick(tradingActivity);
    }

    private void downTick(TradingActivity tradingActivity) {
        currentPriceInSubunits = currentPriceInSubunits - 1;
        tick(tradingActivity);
    }

    private void noTick(TradingActivity tradingActivity) {
        tick(tradingActivity);
    }

    private void tick(TradingActivity tradingActivity) {
        tradeMessageQueue.publish(createMessage(currentPriceInSubunits));
        if (tradingActivity == null) {
            assertThat(receivedSignals).hasSize(0);
        } else {
            assertThat(receivedSignals).hasSize(1);
            SignalMessage signalMessage = receivedSignals.get(0);
            assertThat(signalMessage.getTradingActivity()).isEqualTo(tradingActivity);
            receivedSignals.clear();
        }
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
