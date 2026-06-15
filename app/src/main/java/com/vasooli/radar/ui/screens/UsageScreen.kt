package com.vasooli.radar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vasooli.radar.BottomBar
import com.vasooli.radar.Routes
import com.vasooli.radar.ui.AppViewModel
import com.vasooli.radar.ui.DayCell
import com.vasooli.radar.ui.UsageStats
import com.vasooli.radar.ui.theme.Brand
import com.vasooli.radar.ui.theme.BrandDark
import com.vasooli.radar.ui.theme.SafeGreen
import com.vasooli.radar.ui.theme.WatchAmber

@Composable
fun UsageScreen(vm: AppViewModel, nav: NavController) {
    val s by vm.usageStats.collectAsState()
    Scaffold(bottomBar = { BottomBar(nav, Routes.USAGE) }) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Column {
                    Text("వాడకం · Usage", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "How regularly the app is used",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item { StreakHero(s) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    UsageStat("Last 7 days", "${s.last7}/7", Brand, Modifier.weight(1f))
                    UsageStat("Last 14 days", "${s.last14}/14", WatchAmber, Modifier.weight(1f))
                    UsageStat("Total days", "${s.total}", SafeGreen, Modifier.weight(1f))
                }
            }
            item { CalendarCard(s) }
            item { EvidenceHint() }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun StreakHero(s: UsageStats) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Brush.linearGradient(listOf(Brand, BrandDark)))
            .padding(24.dp)
    ) {
        Column {
            Text("ప్రస్తుత streak · Current streak", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Text("${s.streak} 🔥", color = Color.White, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(4.dp))
            Text(
                if (s.streak == 1) "day in a row" else "days in a row",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun UsageStat(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.13f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = accent)
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CalendarCard(s: UsageStats) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("గత 14 రోజులు · Last 14 days", style = MaterialTheme.typography.titleSmall)
            Text("Green = app opened that day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            s.grid.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    week.forEach { cell -> DayDot(cell, Modifier.weight(1f)) }
                    repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DayDot(cell: DayCell, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (cell.used) SafeGreen else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                cell.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (cell.used) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EvidenceHint() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Brand.copy(alpha = 0.10f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Text(
            "Tip: screenshot this screen each week as your usage evidence. It only counts days the app was actually opened.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
