package com.acme.pricing;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

/** Flat tax rates per region code. Rates are fractions (0.0825 = 8.25%). */
public final class TaxRules {
    private static final Map<String, BigDecimal> RATES = Map.of(
            "US-CA", new BigDecimal("0.0725"),
            "US-NY", new BigDecimal("0.0400"),
            "US-TX", new BigDecimal("0.0625"),
            "US-OR", BigDecimal.ZERO,
            "GB", new BigDecimal("0.2000"),
            "DE", new BigDecimal("0.1900")
    );

    private TaxRules() {
    }

    /** Returns the tax rate for a region, or zero for unknown regions. */
    public static BigDecimal rateFor(String region) {
        if (region == null) {
            return BigDecimal.ZERO;
        }
        return RATES.getOrDefault(region.trim().toUpperCase(Locale.ROOT), BigDecimal.ZERO);
    }
}
