package com.sonar.trading.ui;

import com.sonar.trading.currency.CurrencyUtil;
import com.sonar.trading.messages.SignalMessage;
import com.sonar.trading.messages.TradeMessage;
import com.sonar.trading.utils.TimeUtils;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TradeRow extends HBox {
    private Label tradeId;
    private Label timestamp;
    private Label exchange;
    private Label sourceCurrency;
    private Label destinationCurrency;
    private Label amountInSubunits;
    private Label priceInSubunits;
    private Label tradingActivity;


    public TradeRow(TradeMessage tradeMessage) {
        this.tradeId = new Label(tradeMessage.getId());
        this.timestamp = new Label(TimeUtils.formatTimestamp(tradeMessage.getTimestamp()));
        this.exchange = new Label(String.valueOf(tradeMessage.getExchange()));
        this.sourceCurrency = new Label(String.valueOf(tradeMessage.getSourceCurrency()));
        this.destinationCurrency = new Label(String.valueOf(tradeMessage.getDestinationCurrency()));
        this.amountInSubunits = new Label(String.valueOf(tradeMessage.getAmountInSubunits()));
        this.priceInSubunits = new Label(String.valueOf(tradeMessage.getPriceInSubunits()));
        this.tradingActivity = new Label(String.valueOf(tradeMessage.getTradingActivity()));
        this.getChildren().addAll(tradeId, timestamp, exchange, sourceCurrency, destinationCurrency, amountInSubunits, priceInSubunits, tradingActivity);

        addStyleClasses();
    }

    public TradeRow(SignalMessage signalMessage) {
        // Hard coded amount to 1 because we don't actually execute signals.
        long amountInSubnits = CurrencyUtil.unitsToSubunits(BigDecimal.ONE, signalMessage.getSourceCurrency()).longValueExact();
        this.tradeId = new Label(signalMessage.getId());
        this.timestamp = new Label(TimeUtils.formatTimestamp(signalMessage.getTimestamp()));
        this.exchange = new Label(String.valueOf(signalMessage.getExchange()));
        this.sourceCurrency = new Label(String.valueOf(signalMessage.getSourceCurrency()));
        this.destinationCurrency = new Label(String.valueOf(signalMessage.getDestinationCurrency()));
        this.amountInSubunits = new Label(String.valueOf(amountInSubnits));
        this.priceInSubunits = new Label(String.valueOf(signalMessage.getPriceInSubunits()));
        this.tradingActivity = new Label(String.valueOf(signalMessage.getTradingActivity()));
        this.getChildren().addAll(tradeId, timestamp, exchange, sourceCurrency, destinationCurrency, amountInSubunits, priceInSubunits, tradingActivity);

        addStyleClasses();
    }

    private void addStyleClasses() {
        getStyleClass().add("tradesContainer");
    }
}
