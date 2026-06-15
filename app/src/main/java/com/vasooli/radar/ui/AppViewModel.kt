package com.vasooli.radar.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vasooli.radar.data.LedgerEntry
import com.vasooli.radar.data.Repository
import com.vasooli.radar.data.Retailer
import com.vasooli.radar.domain.RiskBand
import com.vasooli.radar.domain.RiskEngine
import com.vasooli.radar.domain.RiskResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RetailerHealth(val retailer: Retailer, val risk: RiskResult)

data class DashboardState(
    val totalOutstanding: Double = 0.0,
    val totalOverdue: Double = 0.0,
    val overdueCount: Int = 0,
    val atRisk: Double = 0.0,
    val safe: Int = 0,
    val watch: Int = 0,
    val high: Int = 0,
    val aging: List<Double> = listOf(0.0, 0.0, 0.0, 0.0),
    val chase: List<RetailerHealth> = emptyList()
)

data class DayCell(val label: String, val used: Boolean)

data class UsageStats(
    val streak: Int = 0,     // current streak, all-time (uncapped)
    val longest: Int = 0,    // best streak ever
    val last7: Int = 0,
    val last14: Int = 0,
    val total: Int = 0,
    val grid: List<DayCell> = emptyList()
)

class AppViewModel(private val repo: Repository) : ViewModel() {

    val health: StateFlow<List<RetailerHealth>> =
        combine(repo.retailers, repo.ledger) { retailers, ledger ->
            val byRetailer = ledger.groupBy { it.retailerId }
            val now = System.currentTimeMillis()
            retailers.map { r ->
                RetailerHealth(r, RiskEngine.evaluate(r, byRetailer[r.id] ?: emptyList(), now))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dashboard: StateFlow<DashboardState> = health.map { list ->
        val aging = DoubleArray(4)
        list.forEach { h -> h.risk.aging.forEachIndexed { i, v -> aging[i] += v } }
        DashboardState(
            totalOutstanding = list.sumOf { it.risk.outstanding },
            totalOverdue = list.sumOf { it.risk.overdue },
            overdueCount = list.count { it.risk.overdue > 0 },
            atRisk = list.sumOf { it.risk.expectedLoss },
            safe = list.count { it.risk.band == RiskBand.SAFE },
            watch = list.count { it.risk.band == RiskBand.WATCH },
            high = list.count { it.risk.band == RiskBand.HIGH },
            aging = aging.toList(),
            chase = list.filter { it.risk.overdue > 0 || it.risk.band != RiskBand.SAFE }
                .sortedByDescending { it.risk.expectedLoss }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    val usageStats: StateFlow<UsageStats> = repo.usageDays
        .map { computeUsage(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UsageStats())

    fun ledgerFor(id: Long) = repo.ledgerFor(id)

    fun addRetailer(shop: String, owner: String, phone: String, area: String, limit: Double, term: Int) =
        viewModelScope.launch {
            repo.addRetailer(
                Retailer(shopName = shop, ownerName = owner, phone = phone, area = area,
                    creditLimit = limit, termDays = term, createdAt = System.currentTimeMillis())
            )
        }

    fun addSale(retailerId: Long, amount: Double, termDays: Int, note: String?) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        repo.addSale(retailerId, amount, now, now + termDays * 86_400_000L, note)
    }

    fun addPayment(retailerId: Long, amount: Double, note: String?) = viewModelScope.launch {
        repo.addPayment(retailerId, amount, System.currentTimeMillis(), note)
    }

    fun addPromise(retailerId: Long, amount: Double, inDays: Int, note: String?) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        repo.addPromise(retailerId, amount, now + inDays * 86_400_000L, now, note)
    }

    fun updateCreditLimit(r: Retailer, newLimit: Double) = viewModelScope.launch {
        repo.updateRetailer(r.copy(creditLimit = newLimit))
    }

    fun editRetailer(r: Retailer) = viewModelScope.launch { repo.updateRetailer(r) }

    fun deleteRetailer(r: Retailer) = viewModelScope.launch { repo.deleteRetailer(r) }

    fun deleteEntry(e: LedgerEntry) = viewModelScope.launch { repo.deleteEntry(e) }

    companion object {
        fun factory(repo: Repository) = viewModelFactory { initializer { AppViewModel(repo) } }
    }
}

/** Most-recent-first list of the last [n] calendar-day keys ("yyyy-MM-dd"). */
private fun lastNDayKeys(n: Int): List<String> {
    val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    val cal = java.util.Calendar.getInstance()
    val out = ArrayList<String>(n)
    repeat(n) {
        out.add(fmt.format(cal.time))
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
    }
    return out
}

/** Shift a "yyyy-MM-dd" key by [delta] days (handles month/year/DST boundaries). */
private fun shiftDay(key: String, delta: Int): String {
    val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    val parsed = fmt.parse(key) ?: return key
    val cal = java.util.Calendar.getInstance()
    cal.time = parsed
    cal.add(java.util.Calendar.DAY_OF_YEAR, delta)
    return fmt.format(cal.time)
}

private fun computeUsage(days: List<String>): UsageStats {
    val used = days.toHashSet()
    val total = used.size

    // Rolling windows (the "5+ days/week" evidence) + the visual calendar.
    val recent = lastNDayKeys(14) // index 0 = today
    val last7 = recent.take(7).count { it in used }
    val last14 = recent.count { it in used }

    // CURRENT streak — all-time, uncapped. Counts from today, or from yesterday
    // if today hasn't been opened yet (a streak only breaks after a full missed day).
    val todayKey = recent.firstOrNull() ?: ""
    var cursor: String? = when {
        todayKey in used -> todayKey
        shiftDay(todayKey, -1) in used -> shiftDay(todayKey, -1)
        else -> null
    }
    var current = 0
    while (cursor != null && cursor in used) {
        current++
        cursor = shiftDay(cursor, -1)
    }

    // LONGEST streak — all-time best run of consecutive days.
    var longest = 0
    for (d in used) {
        if (shiftDay(d, -1) !in used) {          // d starts a run
            var len = 1
            var next = shiftDay(d, 1)
            while (next in used) { len++; next = shiftDay(next, 1) }
            if (len > longest) longest = len
        }
    }

    val grid = recent.reversed().map { key -> DayCell(key.takeLast(2), key in used) }
    return UsageStats(
        streak = current, longest = longest, last7 = last7, last14 = last14,
        total = total, grid = grid
    )
}
