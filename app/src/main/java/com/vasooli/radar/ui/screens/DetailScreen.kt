@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)

package com.vasooli.radar.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vasooli.radar.Routes
import com.vasooli.radar.data.EntryType
import com.vasooli.radar.data.LedgerEntry
import com.vasooli.radar.domain.dayMonth
import com.vasooli.radar.domain.money
import com.vasooli.radar.ui.AppViewModel
import com.vasooli.radar.ui.RetailerHealth
import com.vasooli.radar.ui.components.ReasonChip
import com.vasooli.radar.ui.components.RiskBadge
import com.vasooli.radar.ui.components.RiskGauge
import com.vasooli.radar.ui.components.Sparkline
import com.vasooli.radar.ui.components.dial
import com.vasooli.radar.ui.components.riskColor
import com.vasooli.radar.ui.theme.HighRed
import com.vasooli.radar.ui.theme.SafeGreen
import kotlin.math.roundToInt

@Composable
fun DetailScreen(vm: AppViewModel, nav: NavController, id: Long) {
    val list by vm.health.collectAsState()
    val h = list.find { it.retailer.id == id }
    val ledgerFlow = remember(id) { vm.ledgerFor(id) }
    val entries by ledgerFlow.collectAsState(initial = emptyList())
    val ctx = LocalContext.current

    var menuOpen by remember { mutableStateOf(false) }
    var showDeleteShop by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<LedgerEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(h?.retailer?.shopName ?: "Retailer") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (h != null) {
                        IconButton(onClick = { dial(ctx, h.retailer.phone) }) {
                            Icon(Icons.Filled.Call, "Call")
                        }
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Filled.MoreVert, "More")
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text("ఎడిట్ · Edit") },
                                    onClick = { menuOpen = false; nav.navigate(Routes.editRetailer(h.retailer.id)) },
                                    leadingIcon = { Icon(Icons.Filled.Edit, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("తొలగించు · Delete shop") },
                                    onClick = { menuOpen = false; showDeleteShop = true },
                                    leadingIcon = { Icon(Icons.Filled.Delete, null, tint = HighRed) }
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = { if (h != null) ActionBar(h, nav) }
    ) { pad ->
        if (h == null) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    RiskGauge(h.risk.score, h.risk.band)
                    Spacer(Modifier.height(8.dp))
                    RiskBadge(h.risk.band)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${h.retailer.ownerName} • ${h.retailer.area}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item { ReasonsCard(h) }
            item { MoneyStatsCard(h) }
            item { RecommendationCard(h, vm) }
            item { TrendCard(h) }
            item {
                Column {
                    Text("లెడ్జర్ చరిత్ర · Ledger history", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (entries.isNotEmpty()) {
                        Text(
                            "ఎంట్రీ తొలగించడానికి long-press · long-press an entry to delete",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (entries.isEmpty()) {
                item { Text("No entries yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(entries.sortedByDescending { it.date }, key = { it.id }) { e ->
                    LedgerRow(e, onLongClick = { entryToDelete = e })
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }

        if (showDeleteShop) {
            AlertDialog(
                onDismissRequest = { showDeleteShop = false },
                title = { Text("దుకాణం తొలగించాలా? · Delete shop?") },
                text = { Text("${h.retailer.shopName} మరియు దాని మొత్తం లెడ్జర్ తీసివేయబడుతుంది.\nThis removes the shop and its entire ledger. Cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = { vm.deleteRetailer(h.retailer); showDeleteShop = false; nav.popBackStack() }) {
                        Text("Delete", color = HighRed)
                    }
                },
                dismissButton = { TextButton(onClick = { showDeleteShop = false }) { Text("Cancel") } }
            )
        }
        val del = entryToDelete
        if (del != null) {
            AlertDialog(
                onDismissRequest = { entryToDelete = null },
                title = { Text("ఎంట్రీ తొలగించాలా? · Delete entry?") },
                text = { Text("${money(del.amount)} · ${dayMonth(del.date)}\nScore will be recalculated.") },
                confirmButton = {
                    TextButton(onClick = { vm.deleteEntry(del); entryToDelete = null }) {
                        Text("Delete", color = HighRed)
                    }
                },
                dismissButton = { TextButton(onClick = { entryToDelete = null }) { Text("Cancel") } }
            )
        }
    }
}

@Composable
private fun ReasonsCard(h: RetailerHealth) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("ఈ స్కోర్ ఎందుకు · Why this score", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                h.risk.reasons.forEach { ReasonChip(it) }
            }
        }
    }
}

@Composable
private fun MoneyStatsCard(h: RetailerHealth) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatCell("Outstanding", money(h.risk.outstanding), MaterialTheme.colorScheme.onSurface)
            StatCell("Overdue", money(h.risk.overdue), if (h.risk.overdue > 0) HighRed else MaterialTheme.colorScheme.onSurface)
            StatCell("Limit used", "${(h.risk.utilization * 100).roundToInt()}%", riskColor(h.risk.band))
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RecommendationCard(h: RetailerHealth, vm: AppViewModel) {
    val rec = h.risk.recommendedLimit
    val current = h.retailer.creditLimit
    val raise = rec >= current
    val accent = if (raise) SafeGreen else HighRed
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("సూచించిన లిమిట్ · Suggested credit limit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(money(current), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("  →  ", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(money(rec), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                if (raise) "Reliable payer — safe to extend more stock on credit."
                else "Risk rising — tighten the limit to cap your exposure.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { vm.updateCreditLimit(h.retailer, rec) },
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) { Text("Apply ${money(rec)} limit") }
        }
    }
}

@Composable
private fun TrendCard(h: RetailerHealth) {
    val up = h.risk.orderTrendPct >= 0
    val c = if (up) SafeGreen else HighRed
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Purchases — last 6 months", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Shrinking orders = a shop in trouble", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(if (up) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown, null, tint = c)
                Spacer(Modifier.width(4.dp))
                Text("${if (up) "+" else ""}${h.risk.orderTrendPct}%", color = c, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Sparkline(h.risk.monthlyPurchases, riskColor(h.risk.band), Modifier.fillMaxWidth().height(70.dp))
        }
    }
}

@Composable
private fun LedgerRow(e: LedgerEntry, onLongClick: () -> Unit) {
    val (icon, color, label, sign) = when (e.type) {
        EntryType.SALE -> LedgerStyle(Icons.Filled.ShoppingCart, MaterialTheme.colorScheme.onSurface, "Stock given", "+")
        EntryType.PAYMENT -> LedgerStyle(Icons.Filled.Payments, SafeGreen, "Payment received", "−")
        EntryType.PROMISE -> LedgerStyle(Icons.Filled.Schedule, MaterialTheme.colorScheme.secondary, "Promised to pay", "")
    }
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            val sub = when (e.type) {
                EntryType.PROMISE -> "by ${e.dueDate?.let { dayMonth(it) } ?: "-"}"
                else -> e.note ?: dayMonth(e.date)
            }
            Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("$sign${money(e.amount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
            Text(dayMonth(e.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class LedgerStyle(val icon: ImageVector, val color: Color, val label: String, val sign: String)

@Composable
private fun ActionBar(h: RetailerHealth, nav: NavController) {
    Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { nav.navigate(Routes.entry(h.retailer.id, "sale")) },
                modifier = Modifier.weight(1f)
            ) { Text("Sale") }
            Button(
                onClick = { nav.navigate(Routes.entry(h.retailer.id, "payment")) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen)
            ) { Text("Payment") }
            OutlinedButton(
                onClick = { nav.navigate(Routes.entry(h.retailer.id, "promise")) },
                modifier = Modifier.weight(1f)
            ) { Text("Promise") }
        }
    }
}
