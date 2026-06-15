# Vasooli — Collection Radar

**A native Android app that tells a small FMCG cash-&-carry wholesaler *which retailer to chase first*.**

OKCredit and Khatabook *record* who owes what. Vasooli goes one step further: it reads the
wholesaler's own ledger and **scores the credit risk of every retailer customer**, predicts
which dues are about to go bad, and produces a daily, prioritised **chase list** — so a
3–5%-margin business stops one bad debt from wiping out the profit on twenty good sales.

> Built for the OKCredit remote-internship application. Solo build. 100% on-device — no
> WhatsApp API, no hardware, no GST/government integration, no external services.

---

## The problem (why it's real & impactful)

- Quick-commerce (Blinkit/Zepto/Instamart) is shrinking kirana sales — and kiranas are the
  wholesaler's credit customers.
- FMCG distributor margins are only **3.5–5%** and "no longer sustainable" (AICPDF, 2026).
- A typical distributor runs **avg ~28-day receivables vs a 15-day target**, with lakhs of
  rupees locked in the market at any time.
- When a shop's sales fall, it stops paying the wholesaler. He finds out **too late** — his
  only tool is a paper bahi-khata that shows balances, not *trajectories*.

Vasooli turns the ledger he already keeps into an early-warning + collection-prioritisation
system. That's the OKCredit-aligned, differentiated, buildable angle.

---

## The risk engine (transparent, not a black box)

Every retailer gets a **0–100 risk score** from six signals computed **only** from the
wholesaler's own sales/payments — no external data, fully auditable. Each point traces to a
plain-language reason the merchant can see and argue with.

| Signal | Weight | What it captures |
|---|---|---|
| Payment lateness (FIFO-derived) | 28% | How many days late they settle, historically |
| Oldest overdue age | 24% | Money past due, and how stale it is |
| Credit-limit utilisation | 18% | Over-exposed accounts |
| Order recency vs their cadence | 12% | Shop going quiet = trouble |
| Order-size trend | 10% | Shrinking basket = q-commerce pressure |
| Broken payment promises | 8% | Repeatedly says "kal de dunga" |

- **Bands:** 0–33 Safe · 34–66 Watch · 67–100 High Risk.
- **Collection priority** = `risk/100 × money exposed` (expected loss) → that's the chase order.
- **Recommended credit limit** is shaped by the band (extend more for reliable payers, tighten
  for risky ones).

See `domain/RiskEngine.kt` — weights are constants at the top, easy to tune and explain.

---

## Screens

1. **Radar (dashboard)** — money in the market, overdue, at-risk, ageing chart, and *Today's
   Chase List* (ranked, one-tap call).
2. **Retailers** — searchable/sortable book of customers with risk badges + purchase sparklines.
3. **Retailer detail** — risk gauge, "why this score" reasons, money stats, suggested credit
   limit (one-tap apply), 6-month purchase trend, full ledger history, and quick actions.
4. **Add retailer / Record sale · payment · promise** — simple forms.

Calling uses the system dialer (`ACTION_DIAL`) — no permission, no telephony API.

---

## Tech

Kotlin · Jetpack Compose (Material 3) · Room (on-device SQLite) · Navigation-Compose · MVVM.
Hand-drawn Canvas charts (no chart library). Demo data seeds on first launch.

```
app/src/main/java/com/vasooli/radar/
├─ data/        Room: Retailer, LedgerEntry, DAOs, AppDatabase, Repository, SeedData
├─ domain/      RiskEngine (scoring) + Format (₹ helpers)
├─ ui/
│  ├─ theme/    colours + Material3 theme (light/dark)
│  ├─ components/ RiskGauge, Sparkline, AgingChart, RiskBadge, ReasonChip …
│  ├─ screens/  Dashboard, Retailers, Detail, Entry forms
│  └─ AppViewModel.kt
└─ MainActivity.kt  (nav graph + bottom bar)
```

---

## Build & run

**Easiest — Android Studio** (Koala/Ladybug or newer):
1. *File → Open* → select this `EDP` folder.
2. Let Gradle sync (it downloads the wrapper, SDK 35, and dependencies).
3. Pick an emulator or device (API 24+) and press **Run**.

**Command line** (needs JDK 17 + Android SDK; from the project root):
```
gradle wrapper        # first time only, generates ./gradlew
./gradlew assembleDebug
```
The APK lands in `app/build/outputs/apk/debug/`.

> First launch seeds 8 realistic retailers (Safe / Watch / High-Risk) so every screen is
> populated for a demo. Clear app data to reset.
