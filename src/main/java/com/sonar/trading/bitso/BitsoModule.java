package com.sonar.trading.bitso;

import com.sonar.trading.bitso.client.BitsoClient;
import com.sonar.trading.config.AppConfig;
import com.sonar.trading.config.BitsoConfig;
import com.sonar.trading.messages.Exchange;
import com.sonar.trading.messages.ExchangeMessage;
import com.sonar.trading.messages.ExchangeMessageType;
import com.sonar.trading.messages.TradeMessage;
import com.sonar.trading.queue.Queue;
import com.sonar.trading.queue.QueueFactory;
import com.sonar.trading.utils.ApplicationLifecycleManager;
import lombok.Getter;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

@Getter
public class BitsoModule {

    private Map<String, Queue<TradeMessage>> tradeQueuesPerBook;

    public void init(AppConfig appConfig, ScheduledExecutorService executorService, ApplicationLifecycleManager applicationLifecycleManager, QueueFactory<ExchangeMessage> queueFactory) {
        BitsoConfig bitsoConfig = appConfig.getBitso();
        tradeQueuesPerBook = new HashMap<>();
        for (String book: bitsoConfig.getBooks()) {
            tradeQueuesPerBook.put(book, queueFactory.createQueue(Exchange.BITSO, ExchangeMessageType.TRADE, book, TradeMessage.class));
        }

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CloseableHttpClient httpClient = httpClientBuilder.build();
        BitsoClient bitsoClient = new BitsoClient(bitsoConfig.toBitsoClientConfig(), httpClient);
        BitsoQueueProducer bitsoQueueProducer = new BitsoQueueProducer(tradeQueuesPerBook, bitsoClient, bitsoConfig, executorService);

        applicationLifecycleManager.addListener(bitsoQueueProducer);
    }
}
