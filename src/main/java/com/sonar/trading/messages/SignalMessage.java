package com.sonar.trading.messages;

import com.sonar.trading.currency.Currency;
import lombok.Value;

@Value
public class SignalMessage implements ExchangeMessage {
    private final String id;
    private final long timestamp;
    private final Exchange exchange;
    private final Currency sourceCurrency;
    private final Currency destinationCurrency;
    private final long priceInSubunits;
    private final TradingActivity tradingActivity;

    @Override
    public ExchangeMessageType getMessageType() {
        return ExchangeMessageType.TRADE;
    }
}
