@file:OptIn(ExperimentalMaterial3Api::class)

package com.vasooli.radar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vasooli.radar.BottomBar
import com.vasooli.radar.Routes
import com.vasooli.radar.domain.money
import com.vasooli.radar.ui.AppViewModel
import com.vasooli.radar.ui.DashboardState
import com.vasooli.radar.ui.RetailerHealth
import com.vasooli.radar.ui.components.AgingChart
import com.vasooli.radar.ui.components.ReasonChip
import com.vasooli.radar.ui.components.dial
import com.vasooli.radar.ui.components.riskColor
import com.vasooli.radar.ui.theme.Brand
import com.vasooli.radar.ui.theme.BrandDark
import com.vasooli.radar.ui.theme.Coral
import com.vasooli.radar.ui.theme.HighRed
import com.vasooli.radar.ui.theme.SafeGreen
import com.vasooli.radar.ui.theme.WatchAmber

@Composable
fun DashboardScreen(vm: AppViewModel, nav: NavController) {
    val state by vm.dashboard.collectAsState()
    Scaffold(bottomBar = { BottomBar(nav, Routes.DASHBOARD) }) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { Header() }
            item { HeroCard(state) }
            item { StatRow(state) }
            item { AgingCard(state) }
            item { SectionTitle("ఈరోజు వసూలు లిస్ట్", "Today's Chase List") }
            if (state.chase.isEmpty()) {
                item { AllClearCard() }
            } else {
                itemsIndexed(state.chase) { i, h -> ChaseCard(i + 1, h, nav) }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun Header() {
    Column {
        Text("నమస్కారం 👋", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(2.dp))
        Text(
            "ఈరోజు ఎవరి దగ్గర వసూలు చేయాలి? · Who to collect from today?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionTitle(te: String, en: String) {
    Column {
        Text(te, style = MaterialTheme.typography.titleMedium)
        Text(en, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HeroCard(s: DashboardState) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(listOf(Brand, BrandDark)))
            .padding(24.dp)
    ) {
        Column {
            Text("మార్కెట్‌లో డబ్బు · Money in the market", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(10.dp))
            Text(money(s.totalOutstanding), color = Color.White, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HeroChip("బాకీ · Overdue", money(s.totalOverdue), "${s.overdueCount} shops", Modifier.weight(1f))
                HeroChip("రిస్క్ · At risk", money(s.atRisk), "act now", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroChip(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(14.dp)
    ) {
        Text(label, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(3.dp))
        Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        Text(sub, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun StatRow(s: DashboardState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MiniStat("High", "ఎక్కువ రిస్క్", s.high.toString(), HighRed, Modifier.weight(1f))
        MiniStat("Watch", "గమనించు", s.watch.toString(), WatchAmber, Modifier.weight(1f))
        MiniStat("Safe", "సురక్షితం", s.safe.toString(), SafeGreen, Modifier.weight(1f))
    }
}

@Composable
private fun MiniStat(en: String, te: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.13f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = accent)
            Spacer(Modifier.height(2.dp))
            Text(en, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text(te, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AgingCard(s: DashboardState) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("బాకీ ఎంత పాతది · Overdue ageing", style = MaterialTheme.typography.titleSmall)
            Text("Days past due date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            AgingChart(s.aging, Modifier.fillMaxWidth().height(120.dp))
        }
    }
}

@Composable
private fun ChaseCard(rank: Int, h: RetailerHealth, nav: NavController) {
    val ctx = LocalContext.current
    val c = riskColor(h.risk.band)
    Card(
        onClick = { nav.navigate(Routes.detail(h.retailer.id)) },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(c.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Text("$rank", color = c, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(h.retailer.shopName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(h.retailer.area, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        money(if (h.risk.overdue > 0) h.risk.overdue else h.risk.outstanding),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = c
                    )
                    if (h.risk.oldestOverdueDays > 0) {
                        Text("${h.risk.oldestOverdueDays}d overdue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ReasonChip(h.risk.reasons.first())
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { dial(ctx, h.retailer.phone) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Coral, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Call, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Call")
                }
            }
        }
    }
}

@Composable
private fun AllClearCard() {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SafeGreen.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(Modifier.padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, null, tint = SafeGreen)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("అంతా క్లియర్! · All clear!", style = MaterialTheme.typography.titleSmall)
                Text("ఈరోజు బాకీ లేదు · No overdue accounts today.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
