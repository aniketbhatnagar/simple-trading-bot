package com.sonar.trading.config;

import lombok.Data;

@Data
public class TradingConfig {
    private int numUpTicksToPlaceSellOrder;
    private int numDownTicksToPlaceBuyOrder;
}
