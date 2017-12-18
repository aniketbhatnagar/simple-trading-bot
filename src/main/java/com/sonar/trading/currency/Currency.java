package com.sonar.trading.currency;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Currency {
    BTC(8),
    MXN(2);

    private final int subunits;
}
