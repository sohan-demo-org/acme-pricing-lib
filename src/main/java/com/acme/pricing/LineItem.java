package com.acme.pricing;

import java.math.BigDecimal;
import java.util.Objects;

/** A single purchasable line: SKU, unit price, currency and quantity. */
public final class LineItem {
    private final String sku;
    private final BigDecimal unitPrice;
    private final String currency;
    private final int quantity;

    /**
     * @param currency ISO 4217 code such as "USD" or "GBP"; every item in an order must share one currency
     */
    public LineItem(String sku, BigDecimal unitPrice, String currency, int quantity) {
        this.sku = Objects.requireNonNull(sku, "sku");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice");
        this.currency = Objects.requireNonNull(currency, "currency");
        if (currency.length() != 3) {
            throw new IllegalArgumentException("currency must be a 3-letter ISO 4217 code: " + currency);
        }
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice must not be negative: " + unitPrice);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }
        this.quantity = quantity;
    }

    public String sku() {
        return sku;
    }

    public BigDecimal unitPrice() {
        return unitPrice;
    }

    public String currency() {
        return currency;
    }

    public int quantity() {
        return quantity;
    }

    /** unitPrice * quantity, unrounded. */
    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineItem)) return false;
        LineItem that = (LineItem) o;
        return quantity == that.quantity && sku.equals(that.sku) && currency.equals(that.currency)
                && unitPrice.compareTo(that.unitPrice) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, unitPrice.stripTrailingZeros(), currency, quantity);
    }

    @Override
    public String toString() {
        return "LineItem{" + sku + " x" + quantity + " @ " + unitPrice + " " + currency + "}";
    }
}
