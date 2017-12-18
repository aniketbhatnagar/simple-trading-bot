package com.sonar.trading.currency;

import java.math.BigDecimal;

public class CurrencyUtil {
    public static BigDecimal unitsToSubunits(BigDecimal units, Currency currency) {
        int currencySubunits = currency.getSubunits();
        return units.movePointRight(currencySubunits);
    }

    public static BigDecimal subunitsToUnits(Long lSubunits, Currency currency) {
        int currencySubunits = currency.getSubunits();
        BigDecimal subunits = new BigDecimal(lSubunits);
        return subunits.movePointLeft(currencySubunits);
    }
}
