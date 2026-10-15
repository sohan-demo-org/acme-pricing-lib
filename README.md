# acme-pricing-lib

Shared pricing engine for Acme services, published as `com.acme:pricing` to GitHub Packages.

```xml
<dependency>
  <groupId>com.acme</groupId>
  <artifactId>pricing</artifactId>
  <version>1.0.0</version>
</dependency>
```

Consumers: [acme-checkout-service](https://github.com/sohan-demo-org/acme-checkout-service),
[acme-invoicing-worker](https://github.com/sohan-demo-org/acme-invoicing-worker).

## Usage

```java
PricingEngine engine = new PricingEngine();
List<LineItem> items = List.of(new LineItem("SKU-1", new BigDecimal("19.99"), 2));
BigDecimal total = engine.calculateTotal(items, TaxRules.rateFor("US-CA"), DiscountCode.percent("TEN", BigDecimal.TEN));
```

## Releasing

1. Update `CHANGELOG.md` and bump `<version>` in `pom.xml` on `main`.
2. Create a GitHub Release with tag `vX.Y.Z`.
3. `.github/workflows/release.yml` publishes the jar to GitHub Packages and notifies Devin,
   which opens upgrade PRs in every consumer repository.

To replay a release on demand, run the **Release** workflow from the Actions tab (`workflow_dispatch`).
