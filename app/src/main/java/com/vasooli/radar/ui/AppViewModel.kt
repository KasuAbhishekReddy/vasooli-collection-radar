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
