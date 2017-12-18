package com.sonar.trading.messages;

public interface ExchangeMessage {
    String getId();
    long getTimestamp();
    ExchangeMessageType getMessageType();
}
