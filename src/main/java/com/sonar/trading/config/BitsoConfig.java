package com.sonar.trading.config;

import com.sonar.trading.bitso.client.BitsoClientConfig;
import lombok.Data;

import java.util.List;

@Data
public class BitsoConfig {
    private String endpoint;
    private List<String> books;
    private int maxTradesToFetch;
    private long tradesPollingDelayMs;

    public BitsoClientConfig toBitsoClientConfig() {
        return new BitsoClientConfig(endpoint);
    }
}
