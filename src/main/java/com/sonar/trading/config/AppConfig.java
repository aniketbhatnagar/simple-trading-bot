package com.sonar.trading.config;

import lombok.Data;

@Data
public class AppConfig {
    private int exchangePollerThreadPoolSize;
    private BitsoConfig bitso;
    private TradingConfig trading;
    private UIConfig ui;
}
