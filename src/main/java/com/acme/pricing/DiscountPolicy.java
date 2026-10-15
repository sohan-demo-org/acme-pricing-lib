package com.acme.pricing;

import java.math.BigDecimal;

/**
 * Applies {@link DiscountCode}s to amounts. In 1.x this logic lived on
 * {@code PricingEngine.applyDiscount}; it is now a separate, pluggable policy.
 */
public class DiscountPolicy {
    /** The amount to subtract for a discount code (never more than the amount itself). */
    public BigDecimal reductionFor(BigDecimal amount, DiscountCode code) {
        if (code == null) {
            return PricingEngine.round(BigDecimal.ZERO);
        }
        BigDecimal reduction;
        switch (code.kind()) {
            case PERCENT:
                reduction = amount.multiply(code.value()).divide(BigDecimal.valueOf(100), 10, PricingEngine.ROUNDING);
                break;
            case FIXED:
                reduction = code.value();
                break;
            default:
                throw new IllegalStateException("unknown discount kind " + code.kind());
        }
        return PricingEngine.round(reduction.min(amount));
    }

    /** The amount after the discount is applied (never below zero). */
    public BigDecimal apply(BigDecimal amount, DiscountCode code) {
        return PricingEngine.round(amount.subtract(reductionFor(amount, code)));
    }
}
