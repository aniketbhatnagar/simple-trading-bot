package com.sonar.trading.bitso.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sonar.trading.currency.Currency;
import com.sonar.trading.currency.CurrencyUtil;
import com.sonar.trading.messages.Exchange;
import com.sonar.trading.messages.TradeMessage;
import com.sonar.trading.messages.TradingActivity;
import com.sonar.trading.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.Value;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Value
@AllArgsConstructor(onConstructor=@__(@JsonCreator))
@ToString
public class Trade {

    private static Map<String, TradingActivity> markerSideToTradingActivity = new HashMap<>();
    private static Map<String, Currency> currencyMapper = new HashMap<>();

    {
        markerSideToTradingActivity.put("buy", TradingActivity.BUY);
        markerSideToTradingActivity.put("sell", TradingActivity.SELL);

        currencyMapper.put("btc", Currency.BTC);
        currencyMapper.put("mxn", Currency.MXN);
    }

    private final String book;
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private final ZonedDateTime createdAt;
    private final String amount;
    @JsonProperty("maker_side")
    private final String markerSide;
    private final String price;
    private final long tid;

    public TradeMessage toTradeMessage() {
        String[] bookSplits = book.split("_");
        Currency sourceCurrency = parseCurrency(bookSplits[0]);
        Currency destinationCurrency = parseCurrency(bookSplits[1]);
        long amountInSubunits = parseCurrencyAmount(amount, sourceCurrency);
        long priceInSubunits = parseCurrencyAmount(price, destinationCurrency);
        return TradeMessage.builder()
                    .id(String.valueOf(tid))
                    .timestamp(TimeUtils.toTimestamp(createdAt))
                    .exchange(Exchange.BITSO)
                    .sourceCurrency(sourceCurrency)
                    .destinationCurrency(destinationCurrency)
                    .amountInSubunits(amountInSubunits)
                    .priceInSubunits(priceInSubunits)
                    .tradingActivity(parseTradingActivity(markerSide))
                    .build();
        }

    private Currency parseCurrency(String currencyStr) {
        Currency currency = currencyMapper.get(currencyStr);
        if (currency == null) {
            throw new IllegalArgumentException(String.format("Unknown currency %s in trade %s", currencyStr, this));
        }
        return currency;
    }

    private TradingActivity parseTradingActivity(String markerSide) {
        TradingActivity tradingActivity = markerSideToTradingActivity.get(markerSide);
        if (tradingActivity == null) {
            throw new IllegalArgumentException(String.format("Unknown markerSide %s in trade %s", markerSide, this));
        }
        return tradingActivity;
    }

    private long parseCurrencyAmount(String amount, Currency currency) {
        BigDecimal parsedAmount = new BigDecimal(amount);
        BigDecimal parsedAmountInSubunits = CurrencyUtil.unitsToSubunits(parsedAmount, currency);
        try {
            long amountInSubunits = parsedAmountInSubunits.longValueExact();
            if (amountInSubunits < 0) {
                throw new IllegalArgumentException(String.format("Parsed amount %d from %s is negative in trade %s", amountInSubunits, amount, this));
            }
            return amountInSubunits;
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(String.format("Parsed amount %s from %s needs rounding in trade %s. Please check currency units to subunits logic.", parsedAmountInSubunits, amount, this), e);
        }
    }
}
