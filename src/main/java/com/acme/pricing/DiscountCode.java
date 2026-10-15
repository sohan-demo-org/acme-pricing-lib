package com.acme.pricing;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A promotional code. Either a percentage off the subtotal (e.g. 10 = 10%)
 * or a fixed amount off (e.g. 5.00).
 */
public final class DiscountCode {
    public enum Kind { PERCENT, FIXED }

    private final String code;
    private final Kind kind;
    private final BigDecimal value;

    private DiscountCode(String code, Kind kind, BigDecimal value) {
        this.code = Objects.requireNonNull(code, "code");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.value = Objects.requireNonNull(value, "value");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("discount value must not be negative: " + value);
        }
        if (kind == Kind.PERCENT && value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("percent discount cannot exceed 100: " + value);
        }
    }

    public static DiscountCode percent(String code, BigDecimal percent) {
        return new DiscountCode(code, Kind.PERCENT, percent);
    }

    public static DiscountCode fixed(String code, BigDecimal amount) {
        return new DiscountCode(code, Kind.FIXED, amount);
    }

    public String code() {
        return code;
    }

    public Kind kind() {
        return kind;
    }

    public BigDecimal value() {
        return value;
    }

    @Override
    public String toString() {
        return code + "(" + kind + " " + value + ")";
    }
}
