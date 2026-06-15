@file:OptIn(ExperimentalMaterial3Api::class)

package com.vasooli.radar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vasooli.radar.BottomBar
import com.vasooli.radar.Routes
import com.vasooli.radar.domain.money
import com.vasooli.radar.ui.AppViewModel
import com.vasooli.radar.ui.RetailerHealth
import com.vasooli.radar.ui.components.InitialsAvatar
import com.vasooli.radar.ui.components.RiskBadge
import com.vasooli.radar.ui.components.Sparkline
import com.vasooli.radar.ui.components.riskColor
import com.vasooli.radar.ui.theme.Brand

private enum class Sort { RISK, OUTSTANDING, NAME }

@Composable
fun RetailersScreen(vm: AppViewModel, nav: NavController) {
    val list by vm.health.collectAsState()
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(Sort.RISK) }

    val filtered = remember(list, query, sort) {
        list.filter {
            it.retailer.shopName.contains(query, true) ||
                it.retailer.ownerName.contains(query, true) ||
                it.retailer.area.contains(query, true)
        }.sortedWith(
            when (sort) {
                Sort.RISK -> compareByDescending<RetailerHealth> { it.risk.score }
                Sort.OUTSTANDING -> compareByDescending<RetailerHealth> { it.risk.outstanding }
                Sort.NAME -> compareBy<RetailerHealth> { it.retailer.shopName }
            }
        )
    }

    Scaffold(
        bottomBar = { BottomBar(nav, Routes.RETAILERS) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate(Routes.ADD_RETAILER) },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Add shop") },
                containerColor = Brand,
                contentColor = Color.White
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(8.dp))
            Text("దుకాణాలు", style = MaterialTheme.typography.headlineSmall)
            Text("Retailers", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search shop, owner or area") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortChip("Risk", sort == Sort.RISK) { sort = Sort.RISK }
                SortChip("Outstanding", sort == Sort.OUTSTANDING) { sort = Sort.OUTSTANDING }
                SortChip("Name", sort == Sort.NAME) { sort = Sort.NAME }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Text(
                            if (query.isBlank()) "దుకాణాలు లేవు · No retailers yet.\nTap + to add your first shop."
                            else "ఏ దుకాణం దొరకలేదు · No match found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 56.dp)
                        )
                    }
                } else {
                    items(filtered, key = { it.retailer.id }) { h -> RetailerRow(h, nav) }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Brand.copy(alpha = 0.15f),
            selectedLabelColor = Brand
        )
    )
}

@Composable
private fun RetailerRow(h: RetailerHealth, nav: NavController) {
    val c = riskColor(h.risk.band)
    Card(
        onClick = { nav.navigate(Routes.detail(h.retailer.id)) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            InitialsAvatar(h.retailer.shopName, c)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(h.retailer.shopName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text("${money(h.risk.outstanding)} outstanding", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                RiskBadge(h.risk.band)
            }
            Spacer(Modifier.width(8.dp))
            Sparkline(h.risk.monthlyPurchases, c, Modifier.width(58.dp).height(34.dp))
        }
    }
}
