# Changelog

All notable changes to `com.acme:pricing` are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/) and this project uses [Semantic Versioning](https://semver.org/).

## [2.0.0] - 2026-10-15

### Breaking changes
- `PricingEngine.calculateTotal(...)` now returns a `PriceBreakdown` (`currency`, `subtotal`, `discount`, `tax`, `total`)
  instead of a bare `BigDecimal`. Callers that need the old value should use `calculateTotal(...).total()`.
- `PricingEngine.applyDiscount(amount, code)` has been removed. Use `new DiscountPolicy().apply(amount, code)`,
  or read the applied amount from `PriceBreakdown.discount()`.
- `LineItem` now requires a currency: `new LineItem(sku, unitPrice, currency, quantity)` (ISO 4217, e.g. `"USD"`).
  `calculateTotal` rejects empty orders and orders that mix currencies with `IllegalArgumentException`.

### Migration guide
```java
// 1.x
BigDecimal total = engine.calculateTotal(items, taxRate, code);
BigDecimal afterDiscount = engine.applyDiscount(subtotal, code);
LineItem item = new LineItem("SKU-1", price, 2);

// 2.x
PriceBreakdown breakdown = engine.calculateTotal(items, taxRate, code);
BigDecimal total = breakdown.total();
BigDecimal afterDiscount = new DiscountPolicy().apply(subtotal, code);
LineItem item = new LineItem("SKU-1", price, "USD", 2);
```
Consumers should surface `breakdown.tax()` and `breakdown.discount()` where they previously only showed a total.

### Added
- `PriceBreakdown` value type and `DiscountPolicy` (injectable via `new PricingEngine(DiscountPolicy)`).

## [1.0.0] - 2026-10-14

### Added
- `PricingEngine` with `subtotal`, `applyDiscount` and `calculateTotal`.
- `LineItem`, `DiscountCode` (percent / fixed) and `TaxRules` region rates.
