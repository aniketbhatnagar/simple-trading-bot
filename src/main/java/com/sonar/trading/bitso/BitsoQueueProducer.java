package com.sonar.trading.bitso;

import com.sonar.trading.bitso.client.BitsoClient;
import com.sonar.trading.bitso.client.BitsoResponse;
import com.sonar.trading.bitso.client.Sort;
import com.sonar.trading.bitso.client.Trade;
import com.sonar.trading.config.BitsoConfig;
import com.sonar.trading.messages.TradeMessage;
import com.sonar.trading.queue.Queue;
import com.sonar.trading.utils.ApplicationLifecycleListener;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class BitsoQueueProducer implements ApplicationLifecycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger("bitso");

    private final Map<String, Queue<TradeMessage>> tradeQueuesPerBook;
    private final BitsoClient bitsoClient;
    private final BitsoConfig bitsoConfig;
    private final ScheduledExecutorService executorService;

    private Map<String, Long> markersPerBook;
    private List<ScheduledFuture<?>> scheduledFutures;

    @Override
    public void onStart() {
        long pollingDelayMs = bitsoConfig.getTradesPollingDelayMs();
        scheduledFutures = new ArrayList<>(tradeQueuesPerBook.size());
        markersPerBook = new HashMap<>(tradeQueuesPerBook.size());
        for(Map.Entry<String, Queue<TradeMessage>> bookAndQueue: tradeQueuesPerBook.entrySet()) {
            ScheduledFuture<?> scheduledFuture = executorService.scheduleWithFixedDelay(() -> pollBitsoForTrades(bookAndQueue.getKey(), bookAndQueue.getValue()), pollingDelayMs, pollingDelayMs, TimeUnit.MILLISECONDS);
            scheduledFutures.add(scheduledFuture);
        }

    }

    @Override
    public void onStop() {
        if (scheduledFutures != null) {
            for (ScheduledFuture<?> scheduledFuture : scheduledFutures) {
                scheduledFuture.cancel(false);
            }
        }
    }

    private void pollBitsoForTrades(String book, Queue<TradeMessage> queue) {
        Long marker = markersPerBook.get(book);
        try {
            LOGGER.debug("Fetching latest trades for book {} since marker {}", book, marker);
            Sort sort = Sort.asc;
            if (marker == null) {
                // If there is no previous marker, then we need to fetch the latest trades. Setting the sort as asc without a marker
                // will make the API return all trades from start.
                sort = Sort.desc;
            }
            BitsoResponse<List<Trade>> tradesResponse = bitsoClient.getTrades(book, marker, sort, bitsoConfig.getMaxTradesToFetch());
            List<Trade> trades = tradesResponse.getPayload();
            if (sort == Sort.desc) {
                trades.sort(Comparator.comparing(Trade::getCreatedAt).thenComparing(Trade::getTid));
            }
            for (Trade trade: trades) {
                TradeMessage tradeMessage = trade.toTradeMessage();
                LOGGER.debug("Publishing trade: {}", tradeMessage);
                queue.publish(tradeMessage);
                if (marker == null || marker < trade.getTid()) {
                    marker = trade.getTid();
                }
            }
            markersPerBook.put(book, marker);
        } catch (Exception e) {
            LOGGER.error(String.format("Exception occurred while getting trades for book %s", book), e);
        }
    }
}
