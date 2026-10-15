package com.acme.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TaxRulesTest {
    @Test
    void knownRegionsHaveRates() {
        assertEquals(new BigDecimal("0.0725"), TaxRules.rateFor("US-CA"));
        assertEquals(new BigDecimal("0.2000"), TaxRules.rateFor("gb"));
    }

    @Test
    void unknownRegionIsZero() {
        assertEquals(BigDecimal.ZERO, TaxRules.rateFor("ZZ"));
        assertEquals(BigDecimal.ZERO, TaxRules.rateFor(null));
    }
}
