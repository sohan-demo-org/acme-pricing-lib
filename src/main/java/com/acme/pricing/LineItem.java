package com.acme.pricing;

import java.math.BigDecimal;
import java.util.Objects;

/** A single purchasable line: SKU, unit price and quantity. */
public final class LineItem {
    private final String sku;
    private final BigDecimal unitPrice;
    private final int quantity;

    public LineItem(String sku, BigDecimal unitPrice, int quantity) {
        this.sku = Objects.requireNonNull(sku, "sku");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unitPrice");
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
        return quantity == that.quantity && sku.equals(that.sku) && unitPrice.compareTo(that.unitPrice) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, unitPrice.stripTrailingZeros(), quantity);
    }

    @Override
    public String toString() {
        return "LineItem{" + sku + " x" + quantity + " @ " + unitPrice + "}";
    }
}
