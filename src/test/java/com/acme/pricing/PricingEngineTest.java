package com.acme.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingEngineTest {
    private final PricingEngine engine = new PricingEngine();

    private static LineItem usd(String sku, String price, int qty) {
        return new LineItem(sku, new BigDecimal(price), "USD", qty);
    }

    @Test
    void subtotalSumsLineTotals() {
        List<LineItem> items = List.of(usd("A", "10.00", 2), usd("B", "4.99", 3));
        assertEquals(new BigDecimal("34.97"), engine.subtotal(items));
    }

    @Test
    void subtotalOfEmptyCartIsZero() {
        assertEquals(new BigDecimal("0.00"), engine.subtotal(List.of()));
    }

    @Test
    void breakdownAppliesDiscountBeforeTax() {
        List<LineItem> items = List.of(usd("A", "100.00", 1));
        PriceBreakdown b = engine.calculateTotal(items, new BigDecimal("0.10"),
                DiscountCode.percent("TEN", BigDecimal.TEN));

        assertEquals("USD", b.currency());
        assertEquals(new BigDecimal("100.00"), b.subtotal());
        assertEquals(new BigDecimal("10.00"), b.discount());
        assertEquals(new BigDecimal("9.00"), b.tax());
        assertEquals(new BigDecimal("99.00"), b.total());
    }

    @Test
    void breakdownWithoutDiscountHasZeroDiscount() {
        List<LineItem> items = List.of(usd("A", "19.99", 2));
        PriceBreakdown b = engine.calculateTotal(items, TaxRules.rateFor("US-CA"));

        assertEquals(new BigDecimal("0.00"), b.discount());
        assertEquals(new BigDecimal("42.88"), b.total());
    }

    @Test
    void fixedDiscountIsCappedAtSubtotal() {
        List<LineItem> items = List.of(usd("A", "3.00", 1));
        PriceBreakdown b = engine.calculateTotal(items, BigDecimal.ZERO,
                DiscountCode.fixed("BIG", new BigDecimal("5.00")));

        assertEquals(new BigDecimal("3.00"), b.discount());
        assertEquals(new BigDecimal("0.00"), b.total());
    }

    @Test
    void rejectsEmptyOrderAndMixedCurrencies() {
        assertThrows(IllegalArgumentException.class, () -> engine.calculateTotal(List.of(), BigDecimal.ZERO));
        List<LineItem> mixed = List.of(usd("A", "1.00", 1), new LineItem("B", BigDecimal.ONE, "GBP", 1));
        assertThrows(IllegalArgumentException.class, () -> engine.calculateTotal(mixed, BigDecimal.ZERO));
    }

    @Test
    void totalRoundsHalfUp() {
        List<LineItem> items = List.of(usd("A", "0.005", 1));
        assertEquals(new BigDecimal("0.01"), engine.calculateTotal(items, BigDecimal.ZERO).total());
    }

    @Test
    void lineItemRejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> usd("A", "1.00", 0));
        assertThrows(IllegalArgumentException.class, () -> usd("A", "-1.00", 1));
        assertThrows(IllegalArgumentException.class, () -> new LineItem("A", BigDecimal.ONE, "US", 1));
    }
}
