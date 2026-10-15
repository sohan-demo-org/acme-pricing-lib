# acme-pricing-lib

The shared **pricing engine** for Acme's commerce platform, published to GitHub Packages as the Maven
artifact `com.acme:pricing`. Every service that needs to turn line items into money — subtotals, promo
discounts, regional sales tax — depends on this library instead of re-implementing the rules.

## How it fits together

```
                 publishes com.acme:pricing              consumes
┌──────────────────────┐   ──────────────────►  ┌────────────────────────────┐
│  acme-pricing-lib    │   (GitHub Packages)    │  acme-checkout-service     │  Spring Boot REST API
│  (this repo)         │                        │  POST /carts/{id}/total    │  prices live shopping carts
│                      │                        └────────────────────────────┘
│  PricingEngine       │                        ┌────────────────────────────┐
│  LineItem            │   ──────────────────►  │  acme-invoicing-worker     │  batch job
│  DiscountCode        │                        │  orders → invoices         │  invoices fulfilled orders
│  TaxRules            │                        └────────────────────────────┘
└──────────────────────┘
          │  GitHub Release / "Run workflow"
          ▼
   release.yml: mvn verify → deploy to GitHub Packages → webhook to Devin
          ▼
   Devin Automation fans out one session per consumer → bumps the version, fixes breakage, opens a PR
```

| Repo | Role | Pin |
|---|---|---|
| [acme-checkout-service](https://github.com/sohan-demo-org/acme-checkout-service) | Spring Boot REST API that prices carts at checkout | `<pricing.version>` in its `pom.xml` |
| [acme-invoicing-worker](https://github.com/sohan-demo-org/acme-invoicing-worker) | Plain-Java batch worker that invoices fulfilled orders | `<pricing.version>` in its `pom.xml` |

## API (1.x)

```java
PricingEngine engine = new PricingEngine();
List<LineItem> items = List.of(new LineItem("SKU-1", new BigDecimal("19.99"), 2));

BigDecimal subtotal = engine.subtotal(items);                                  // 39.98
BigDecimal total    = engine.calculateTotal(items,
                          TaxRules.rateFor("US-CA"),                           // 7.25 %
                          DiscountCode.percent("WELCOME10", BigDecimal.TEN));  // 38.59
```

- `PricingEngine` — `subtotal`, `applyDiscount`, `calculateTotal` (discount first, then tax, HALF_UP to cents)
- `LineItem` — sku, unit price, quantity (validated)
- `DiscountCode` — `percent(code, pct)` or `fixed(code, amount)`
- `TaxRules` — region → tax rate (`US-CA`, `US-NY`, `US-TX`, `US-OR`, `GB`, `DE`)

See [CHANGELOG.md](CHANGELOG.md) for version history and migration notes.

## Consuming it

```xml
<repositories>
  <repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/sohan-demo-org/acme-pricing-lib</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.acme</groupId>
  <artifactId>pricing</artifactId>
  <version>1.0.0</version>
</dependency>
```

GitHub Packages requires authentication even for public packages: a classic PAT with `read:packages`
(set as `GH_PACKAGES_TOKEN`; both consumers ship a `.mvn/settings.xml` that reads it).

## Developing

```bash
mvn verify            # JUnit 5 tests
mvn install           # install a snapshot locally to try against a consumer
```

## Releasing (and what happens next)

1. Merge the change into `main` with a `CHANGELOG.md` entry.
2. **Releases → Draft a new release → tag `vX.Y.Z` → Publish.**
3. [`release.yml`](.github/workflows/release.yml) builds, tests, publishes `com.acme:pricing:X.Y.Z` to
   GitHub Packages (skipping if that version already exists) and POSTs the release notes to a Devin
   Automation webhook.
4. The automation's preflight script fans out one Devin session per consumer. Each session bumps
   `<pricing.version>`, runs `mvn verify`, reads the CHANGELOG, fixes any compile/test breakage, runs the
   application, and opens a PR in that consumer repo with the evidence.

To replay a release on demand (for example on a demo call): **Actions → Release → Run workflow**, with
`version` and `previous_version`. `scripts/demo-reset.sh` closes the previous Devin PRs/branches and can
delete a release + tag. The full walkthrough lives in [DEMO.md](DEMO.md).

### Repository secrets used by the release workflow

| Secret | Purpose |
|---|---|
| `DEVIN_WEBHOOK_URL` | the Devin Automation's incoming-webhook URL |
| `DEVIN_WEBHOOK_SECRET` | sent as `X-Webhook-Secret` so the automation accepts the call |
| `GITHUB_TOKEN` (built-in) | publishes to GitHub Packages |
