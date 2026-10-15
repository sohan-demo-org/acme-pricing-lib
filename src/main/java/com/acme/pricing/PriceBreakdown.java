package com.acme.pricing;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Result of pricing an order: every component that makes up the total.
 * Replaces the bare {@code BigDecimal} returned by {@code calculateTotal} in 1.x.
 */
public final class PriceBreakdown {
    private final String currency;
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal tax;
    private final BigDecimal total;

    public PriceBreakdown(String currency, BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal total) {
        this.currency = Objects.requireNonNull(currency, "currency");
        this.subtotal = Objects.requireNonNull(subtotal, "subtotal");
        this.discount = Objects.requireNonNull(discount, "discount");
        this.tax = Objects.requireNonNull(tax, "tax");
        this.total = Objects.requireNonNull(total, "total");
    }

    /** ISO 4217 currency code shared by all line items, e.g. "USD". */
    public String currency() {
        return currency;
    }

    public BigDecimal subtotal() {
        return subtotal;
    }

    /** Amount removed by the discount policy (positive number, 0.00 when none). */
    public BigDecimal discount() {
        return discount;
    }

    public BigDecimal tax() {
        return tax;
    }

    public BigDecimal total() {
        return total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PriceBreakdown)) return false;
        PriceBreakdown that = (PriceBreakdown) o;
        return currency.equals(that.currency)
                && subtotal.compareTo(that.subtotal) == 0
                && discount.compareTo(that.discount) == 0
                && tax.compareTo(that.tax) == 0
                && total.compareTo(that.total) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, subtotal.stripTrailingZeros(), discount.stripTrailingZeros(),
                tax.stripTrailingZeros(), total.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return "PriceBreakdown{" + currency + " subtotal=" + subtotal + ", discount=" + discount
                + ", tax=" + tax + ", total=" + total + "}";
    }
}
