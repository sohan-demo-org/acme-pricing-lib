package com.acme.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DiscountPolicyTest {
    private final DiscountPolicy policy = new DiscountPolicy();

    @Test
    void percentDiscountReducesAmount() {
        assertEquals(new BigDecimal("90.00"),
                policy.apply(new BigDecimal("100.00"), DiscountCode.percent("TEN", BigDecimal.TEN)));
    }

    @Test
    void fixedDiscountNeverGoesBelowZero() {
        assertEquals(new BigDecimal("0.00"),
                policy.apply(new BigDecimal("3.00"), DiscountCode.fixed("BIG", new BigDecimal("5.00"))));
    }

    @Test
    void nullCodeMeansNoReduction() {
        assertEquals(new BigDecimal("0.00"), policy.reductionFor(new BigDecimal("3.00"), null));
    }
}
