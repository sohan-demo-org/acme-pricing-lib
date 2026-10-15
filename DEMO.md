# Demo runbook: automated cross-repo dependency upgrades

**Story:** `acme-pricing-lib` (this repo) publishes the shared Maven package `com.acme:pricing`.
Two services depend on it: `acme-checkout-service` (Spring Boot REST API) and `acme-invoicing-worker`
(batch worker). When a new version is released, Devin upgrades both consumers, fixes whatever the
new API broke, re-runs the tests and the apps, and opens a PR in each repo.

```
GitHub Release (or "Run workflow")  →  release.yml: mvn verify + deploy to GitHub Packages
        →  POST webhook to Devin Automation  →  Preflight fans out: 1 session per consumer
        →  Devin: bump pom, mvn verify, read CHANGELOG, fix code+tests, run app  →  PR in A and PR in C
```

## One-time setup (already done)
- Repo secrets on this repo: `DEVIN_WEBHOOK_URL`, `DEVIN_WEBHOOK_SECRET` (from the Devin Automation).
- Devin org secrets: `GH_PACKAGES_USER`, `GH_PACKAGES_TOKEN` (PAT with `read:packages`) so sessions can resolve the jar.
- Devin Automation "acme-pricing-lib release → upgrade consumers" (webhook trigger, run as Organization).

## On the call (≈ 10 min)
1. **Set the scene (1 min).** Show `acme-checkout-service` → `pom.xml` pinned to `<pricing.version>1.0.0</pricing.version>`,
   green CI. Show this repo's **Packages** panel with `com.acme.pricing` 1.0.0.
2. **Ship the breaking change (1 min).** Open the `release/v2.0.0` PR here: `calculateTotal` now returns a
   `PriceBreakdown`, `applyDiscount` moved to `DiscountPolicy`, `LineItem` needs a currency. Point at the
   CHANGELOG migration guide. Merge it, then **Releases → Draft a new release → tag `v2.0.0` → Publish**.
   (Alternative, no merge needed: **Actions → Release → Run workflow**, version `2.0.0`, previous `1.0.0`.)
3. **Watch the chain (1–2 min).** Actions tab: build → publish → "Notify Devin". Devin → Automations → the run shows
   2 items. Two sessions appear: one per consumer.
4. **Narrate the checkout session (5–8 min).** Devin bumps the version → `mvn verify` fails to compile (3 errors) →
   reads the release notes → rewrites `CheckoutService` + tests, adds `tax`/`discount` to the JSON response →
   `mvn verify` green → starts Spring Boot, POSTs a cart, pastes the response → opens the PR. Show the PR
   description (library change summary, test counts, app output) and the CI check.
5. **Fan-out (30 s).** Flip to the invoicing-worker PR: same release, different codebase, different fixes.

Talk track: "Nobody wrote a migration ticket. The release itself is the trigger; Devin reads the changelog the
way a teammate would, and the PR arrives with the proof attached. Multiply this by every internal library and
every consumer."

## Re-running it live
- Any time: **Actions → Release → Run workflow** with `version=2.0.0`, `previous_version=1.0.0`. The publish step
  is skipped when the version already exists; the webhook still fires and Devin opens fresh PRs.
- Clean up old PRs/branches first (optional): `scripts/demo-reset.sh` (needs `gh` auth), or
  `scripts/demo-reset.sh --release 2.0.0` to also delete the release + tag so you can re-publish it on stage.
- Backup trigger if GitHub is slow: Devin → Automations → "Run now" does NOT run the fan-out script; use the
  workflow re-run instead, or start a session manually with the prompt in the automation.

## Warm-up variant (optional)
Release `1.1.0` first (non-breaking, e.g. a rounding helper): Devin's PR is just the version bump + green tests,
in ~3 minutes. Then do 2.0.0 for the contrast.

## Known caveats to not overclaim
- GitHub Packages needs a token even for public packages; that is why sessions carry `GH_PACKAGES_TOKEN`.
- The sessions run unattended; on a bad day Devin may open a draft PR with a question instead of a finished one.
- Session wall-clock is 5–10 minutes; have a pre-run PR open in another tab.
