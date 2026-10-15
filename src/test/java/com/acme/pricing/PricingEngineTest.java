package com.acme.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingEngineTest {
    private final PricingEngine engine = new PricingEngine();

    private static LineItem item(String sku, String price, int qty) {
        return new LineItem(sku, new BigDecimal(price), qty);
    }

    @Test
    void subtotalSumsLineTotals() {
        List<LineItem> items = List.of(item("A", "10.00", 2), item("B", "4.99", 3));
        assertEquals(new BigDecimal("34.97"), engine.subtotal(items));
    }

    @Test
    void subtotalOfEmptyCartIsZero() {
        assertEquals(new BigDecimal("0.00"), engine.subtotal(List.of()));
    }

    @Test
    void percentDiscountReducesAmount() {
        BigDecimal result = engine.applyDiscount(new BigDecimal("100.00"),
                DiscountCode.percent("TEN", BigDecimal.TEN));
        assertEquals(new BigDecimal("90.00"), result);
    }

    @Test
    void fixedDiscountNeverGoesBelowZero() {
        BigDecimal result = engine.applyDiscount(new BigDecimal("3.00"),
                DiscountCode.fixed("BIG", new BigDecimal("5.00")));
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    void totalAppliesDiscountBeforeTax() {
        List<LineItem> items = List.of(item("A", "100.00", 1));
        BigDecimal total = engine.calculateTotal(items, new BigDecimal("0.10"),
                DiscountCode.percent("TEN", BigDecimal.TEN));
        // (100 - 10) * 1.10 = 99.00
        assertEquals(new BigDecimal("99.00"), total);
    }

    @Test
    void totalWithoutDiscountUsesTaxRate() {
        List<LineItem> items = List.of(item("A", "19.99", 2));
        assertEquals(new BigDecimal("42.88"), engine.calculateTotal(items, TaxRules.rateFor("US-CA")));
    }

    @Test
    void totalRoundsHalfUp() {
        List<LineItem> items = List.of(item("A", "0.005", 1));
        assertEquals(new BigDecimal("0.01"), engine.calculateTotal(items, BigDecimal.ZERO));
    }

    @Test
    void lineItemRejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> item("A", "1.00", 0));
        assertThrows(IllegalArgumentException.class, () -> item("A", "-1.00", 1));
    }
}
