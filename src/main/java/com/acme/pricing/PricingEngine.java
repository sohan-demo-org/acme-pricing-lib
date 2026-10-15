package com.acme.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Computes order totals. All monetary results are rounded to 2 decimal places
 * using HALF_UP.
 */
public final class PricingEngine {
    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /** Sum of all line totals, rounded. */
    public BigDecimal subtotal(List<LineItem> items) {
        BigDecimal sum = BigDecimal.ZERO;
        for (LineItem item : items) {
            sum = sum.add(item.lineTotal());
        }
        return round(sum);
    }

    /** Applies a discount code to an amount and returns the discounted amount (never below zero). */
    public BigDecimal applyDiscount(BigDecimal amount, DiscountCode code) {
        if (code == null) {
            return round(amount);
        }
        BigDecimal reduction;
        switch (code.kind()) {
            case PERCENT:
                reduction = amount.multiply(code.value()).divide(BigDecimal.valueOf(100), 10, ROUNDING);
                break;
            case FIXED:
                reduction = code.value();
                break;
            default:
                throw new IllegalStateException("unknown discount kind " + code.kind());
        }
        BigDecimal result = amount.subtract(reduction);
        return round(result.signum() < 0 ? BigDecimal.ZERO : result);
    }

    /**
     * Grand total: (subtotal - discount) * (1 + taxRate), rounded.
     *
     * @param items    the order lines
     * @param taxRate  fractional tax rate, e.g. 0.0725
     * @param discount optional discount code, may be null
     */
    public BigDecimal calculateTotal(List<LineItem> items, BigDecimal taxRate, DiscountCode discount) {
        BigDecimal discounted = applyDiscount(subtotal(items), discount);
        BigDecimal tax = discounted.multiply(taxRate == null ? BigDecimal.ZERO : taxRate);
        return round(discounted.add(tax));
    }

    /** Grand total without a discount. */
    public BigDecimal calculateTotal(List<LineItem> items, BigDecimal taxRate) {
        return calculateTotal(items, taxRate, null);
    }

    static BigDecimal round(BigDecimal value) {
        return value.setScale(SCALE, ROUNDING);
    }
}
