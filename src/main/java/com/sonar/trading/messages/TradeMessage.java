package com.sonar.trading.messages;

import com.sonar.trading.currency.Currency;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder=true)
public class TradeMessage implements ExchangeMessage {
    private final String id;
    private final long timestamp;
    private final Exchange exchange;
    private final Currency sourceCurrency;
    private final Currency destinationCurrency;
    private final long amountInSubunits;
    private final long priceInSubunits;
    private final TradingActivity tradingActivity;

    @Override
    public ExchangeMessageType getMessageType() {
        return ExchangeMessageType.TRADE;
    }
}
