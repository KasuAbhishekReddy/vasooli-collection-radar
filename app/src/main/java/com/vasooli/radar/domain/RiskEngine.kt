package com.vasooli.radar.domain

import com.vasooli.radar.data.EntryType
import com.vasooli.radar.data.LedgerEntry
import com.vasooli.radar.data.Retailer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong

enum class RiskBand { SAFE, WATCH, HIGH }

/** severity: 1 = good/info, 2 = caution, 3 = danger */
data class Reason(val text: String, val severity: Int)

data class RiskResult(
    val score: Int,                 // 0..100, higher = riskier
    val band: RiskBand,
    val outstanding: Double,
    val overdue: Double,
    val oldestOverdueDays: Int,
    val avgDaysLate: Int,
    val utilization: Double,        // outstanding / creditLimit
    val recommendedLimit: Double,
    val recencyDays: Int,
    val orderTrendPct: Int,         // negative = shrinking basket
    val brokenPromises: Int,
    val expectedLoss: Double,       // collection-priority weight = P(default proxy) x money exposed
    val aging: List<Double>,        // overdue split: [0-15, 16-30, 31-45, 45+]
    val monthlyPurchases: List<Double>, // last 6 months oldest->newest
    val reasons: List<Reason>
)

/**
 * Transparent, rule-based credit-risk score computed purely from the wholesaler's own
 * ledger — no external data, no black box. Every point on the score traces to a reason
 * the merchant can see and argue with. That auditability is the whole pitch.
 */
object RiskEngine {
    private const val DAY = 86_400_000L

    // Weights (sum = 1.0). Tunable; documented in README.
    private const val W_LATE = 0.28
    private const val W_AGE = 0.24
    private const val W_UTIL = 0.18
    private const val W_RECENCY = 0.12
    private const val W_TREND = 0.10
    private const val W_PROMISE = 0.08

    fun evaluate(r: Retailer, entries: List<LedgerEntry>, now: Long): RiskResult {
        val sorted = entries.sortedBy { it.date }
        val sales = sorted.filter { it.type == EntryType.SALE }
        val payments = sorted.filter { it.type == EntryType.PAYMENT }
        val promises = sorted.filter { it.type == EntryType.PROMISE }

        // ---- FIFO: allocate each payment to the oldest unpaid sale ----
        data class Open(var remaining: Double, val due: Long)
        val open = ArrayDeque<Open>()
        val lateness = ArrayList<Int>()
        var saleIdx = 0
        for (p in payments) {
            while (saleIdx < sales.size && sales[saleIdx].date <= p.date) {
                val s = sales[saleIdx]
                open.addLast(Open(s.amount, s.dueDate ?: (s.date + r.termDays * DAY)))
                saleIdx++
            }
            var pay = p.amount
            while (pay > 0.0 && open.isNotEmpty()) {
                val head = open.first()
                val applied = min(pay, head.remaining)
                head.remaining -= applied
                pay -= applied
                if (head.remaining <= 0.001) {
                    lateness.add(max(0, ((p.date - head.due) / DAY).toInt()))
                    open.removeFirst()
                }
            }
        }
        while (saleIdx < sales.size) {
            val s = sales[saleIdx]
            open.addLast(Open(s.amount, s.dueDate ?: (s.date + r.termDays * DAY)))
            saleIdx++
        }

        val outstanding = open.sumOf { it.remaining }.coerceAtLeast(0.0)
        val overdueOpen = open.filter { it.due < now && it.remaining > 0.001 }
        val overdue = overdueOpen.sumOf { it.remaining }
        val oldestOverdueDays = overdueOpen.minOfOrNull { ((now - it.due) / DAY).toInt() } ?: 0
        val avgDaysLate = if (lateness.isEmpty()) 0 else lateness.average().roundToInt()
        val utilization = if (r.creditLimit > 0) outstanding / r.creditLimit else 0.0

        val aging = DoubleArray(4)
        for (o in overdueOpen) {
            val d = ((now - o.due) / DAY).toInt()
            val idx = when { d <= 15 -> 0; d <= 30 -> 1; d <= 45 -> 2; else -> 3 }
            aging[idx] += o.remaining
        }

        val lastSale = sales.maxByOrNull { it.date }?.date
        val recencyDays = if (lastSale == null) 999 else ((now - lastSale) / DAY).toInt()
        val avgCadence = if (sales.size >= 2)
            ((sales.last().date - sales.first().date) / DAY).toDouble() / (sales.size - 1) else 30.0

        // monthly purchase totals, last 6 months
        val monthly = DoubleArray(6)
        for (s in sales) {
            val mAgo = ((now - s.date) / (30 * DAY)).toInt()
            if (mAgo in 0..5) monthly[5 - mAgo] += s.amount
        }
        val recent = monthly.toList().takeLast(2).average()
        val older = monthly.toList().take(3).average()
        val orderTrendPct = if (older <= 0.0) 0 else (((recent - older) / older) * 100).roundToInt()

        val brokenPromises = promises.count { pr ->
            val pd = pr.dueDate ?: return@count false
            pd < now && payments.none { it.date >= pd }
        }

        // ---- sub-scores, each 0..100 (higher = riskier) ----
        val sLate = min(100.0, avgDaysLate / 20.0 * 100.0)
        val sAge = min(100.0, oldestOverdueDays / 45.0 * 100.0)
        val sUtil = when {
            utilization <= 0.5 -> utilization / 0.5 * 30.0
            utilization <= 1.0 -> 30.0 + (utilization - 0.5) / 0.5 * 40.0
            else -> min(100.0, 70.0 + (utilization - 1.0) / 0.3 * 30.0)
        }
        val sRecency = if (recencyDays > avgCadence * 2)
            min(100.0, recencyDays / (avgCadence * 3) * 100.0) else 0.0
        val sTrend = if (orderTrendPct < 0) min(100.0, -orderTrendPct.toDouble() / 50.0 * 100.0) else 0.0
        val sPromise = min(100.0, brokenPromises * 50.0)

        val score = (sLate * W_LATE + sAge * W_AGE + sUtil * W_UTIL +
                sRecency * W_RECENCY + sTrend * W_TREND + sPromise * W_PROMISE)
            .roundToInt().coerceIn(0, 100)
        val band = when { score >= 67 -> RiskBand.HIGH; score >= 34 -> RiskBand.WATCH; else -> RiskBand.SAFE }

        // recommended credit limit: shaped by behaviour
        val avgMonthly = monthly.toList().filter { it > 0 }.let { if (it.isEmpty()) r.creditLimit else it.average() }
        val factor = when (band) { RiskBand.SAFE -> 1.5; RiskBand.WATCH -> 1.0; RiskBand.HIGH -> 0.5 }
        val recommendedLimit = (Math.round(avgMonthly * (r.termDays / 30.0) * factor / 500.0) * 500.0)
            .coerceAtLeast(0.0)

        val expectedLoss = (score / 100.0) * (if (overdue > 0) overdue else outstanding)

        // ---- human-readable reasons ----
        val reasons = ArrayList<Reason>()
        if (avgDaysLate >= 5) reasons.add(Reason("Pays $avgDaysLate days late on average", if (avgDaysLate >= 12) 3 else 2))
        if (overdue > 0) reasons.add(Reason(
            money(overdue) + " overdue" + if (oldestOverdueDays > 0) " for $oldestOverdueDays days" else "",
            if (oldestOverdueDays >= 30) 3 else 2))
        if (utilization > 1.0) reasons.add(Reason("Over credit limit (${(utilization * 100).roundToInt()}%)", 3))
        else if (utilization >= 0.8) reasons.add(Reason("Near credit limit (${(utilization * 100).roundToInt()}%)", 2))
        if (orderTrendPct <= -20) reasons.add(Reason("Orders down ${-orderTrendPct}% vs earlier", if (orderTrendPct <= -40) 3 else 2))
        val cadenceX2 = (avgCadence * 2).toInt()
        if (recencyDays in cadenceX2..898) reasons.add(Reason("No order in $recencyDays days", 2))
        if (brokenPromises > 0) reasons.add(Reason("Broke $brokenPromises payment promise" + if (brokenPromises > 1) "s" else "", 3))
        if (reasons.isEmpty()) reasons.add(Reason("Pays on time — healthy account", 1))

        return RiskResult(
            score = score, band = band, outstanding = outstanding, overdue = overdue,
            oldestOverdueDays = oldestOverdueDays, avgDaysLate = avgDaysLate, utilization = utilization,
            recommendedLimit = recommendedLimit, recencyDays = recencyDays, orderTrendPct = orderTrendPct,
            brokenPromises = brokenPromises, expectedLoss = expectedLoss, aging = aging.toList(),
            monthlyPurchases = monthly.toList(), reasons = reasons.sortedByDescending { it.severity }
        )
    }
}
