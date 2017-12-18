package com.sonar.trading.bitso.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Bitso response wrapper constaining payload and success.
 * @param <P> Type of payload.
 */
@Value
@AllArgsConstructor(onConstructor=@__(@JsonCreator))
public class BitsoResponse<P> {
    private final boolean success;
    private final P payload;
}
