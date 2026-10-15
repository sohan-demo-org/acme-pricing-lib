package com.acme.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Computes order totals. All monetary results are rounded to 2 decimal places
 * using HALF_UP.
 *
 * <p>2.0 changes: {@link #calculateTotal} returns a {@link PriceBreakdown}
 * instead of a bare total, discounts are applied through a {@link DiscountPolicy},
 * and every {@link LineItem} carries a currency (mixed currencies are rejected).
 */
public final class PricingEngine {
    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final DiscountPolicy discountPolicy;

    public PricingEngine() {
        this(new DiscountPolicy());
    }

    public PricingEngine(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    /** Sum of all line totals, rounded. */
    public BigDecimal subtotal(List<LineItem> items) {
        BigDecimal sum = BigDecimal.ZERO;
        for (LineItem item : items) {
            sum = sum.add(item.lineTotal());
        }
        return round(sum);
    }

    /**
     * Full price breakdown: subtotal, discount, tax on the discounted amount, and total.
     *
     * @param items    the order lines; must be non-empty and share one currency
     * @param taxRate  fractional tax rate, e.g. 0.0725 (null means no tax)
     * @param discount optional discount code, may be null
     * @throws IllegalArgumentException if items is empty or mixes currencies
     */
    public PriceBreakdown calculateTotal(List<LineItem> items, BigDecimal taxRate, DiscountCode discount) {
        String currency = currencyOf(items);
        BigDecimal subtotal = subtotal(items);
        BigDecimal reduction = discountPolicy.reductionFor(subtotal, discount);
        BigDecimal discounted = round(subtotal.subtract(reduction));
        BigDecimal tax = round(discounted.multiply(taxRate == null ? BigDecimal.ZERO : taxRate));
        return new PriceBreakdown(currency, subtotal, reduction, tax, round(discounted.add(tax)));
    }

    /** Full price breakdown without a discount. */
    public PriceBreakdown calculateTotal(List<LineItem> items, BigDecimal taxRate) {
        return calculateTotal(items, taxRate, null);
    }

    private static String currencyOf(List<LineItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("cannot price an empty order");
        }
        String currency = items.get(0).currency();
        for (LineItem item : items) {
            if (!currency.equals(item.currency())) {
                throw new IllegalArgumentException(
                        "all items must share one currency, found " + currency + " and " + item.currency());
            }
        }
        return currency;
    }

    static BigDecimal round(BigDecimal value) {
        return value.setScale(SCALE, ROUNDING);
    }
}
