@file:OptIn(ExperimentalMaterial3Api::class)

package com.vasooli.radar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.vasooli.radar.domain.money
import com.vasooli.radar.ui.AppViewModel
import com.vasooli.radar.ui.theme.SafeGreen

@Composable
fun AddRetailerScreen(vm: AppViewModel, nav: NavController, editId: Long? = null) {
    val list by vm.health.collectAsState()
    val existing = editId?.let { id -> list.find { it.retailer.id == id }?.retailer }
    val editing = editId != null

    var shop by remember(existing?.id) { mutableStateOf(existing?.shopName ?: "") }
    var owner by remember(existing?.id) { mutableStateOf(existing?.ownerName ?: "") }
    var phone by remember(existing?.id) { mutableStateOf(existing?.phone ?: "") }
    var area by remember(existing?.id) { mutableStateOf(existing?.area ?: "") }
    var limit by remember(existing?.id) { mutableStateOf(existing?.creditLimit?.takeIf { it > 0 }?.toLong()?.toString() ?: "") }
    var term by remember(existing?.id) { mutableStateOf((existing?.termDays ?: 15).toString()) }

    val valid = shop.isNotBlank() && phone.isNotBlank()

    FormScaffold(if (editing) "దుకాణం ఎడిట్ · Edit retailer" else "కొత్త దుకాణం · Add retailer", nav) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Field("Shop name", shop, { shop = it })
            Field("Owner name", owner, { owner = it })
            Field("Phone", phone, { phone = it }, KeyboardType.Phone)
            Field("Area / locality", area, { area = it })
            Field("Credit limit (₹)", limit, { limit = it.filter(Char::isDigit) }, KeyboardType.Number)
            Field("Payment term (days)", term, { term = it.filter(Char::isDigit) }, KeyboardType.Number)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (editing && existing != null) {
                        vm.editRetailer(
                            existing.copy(
                                shopName = shop.trim(), ownerName = owner.trim(), phone = phone.trim(),
                                area = area.trim(), creditLimit = limit.toDoubleOrNull() ?: 0.0,
                                termDays = term.toIntOrNull() ?: 15
                            )
                        )
                    } else {
                        vm.addRetailer(
                            shop.trim(), owner.trim(), phone.trim(), area.trim(),
                            limit.toDoubleOrNull() ?: 0.0, term.toIntOrNull() ?: 15
                        )
                    }
                    nav.popBackStack()
                },
                enabled = valid,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (editing) "Save changes" else "Save retailer") }
        }
    }
}

@Composable
fun AddEntryScreen(vm: AppViewModel, nav: NavController, id: Long, kind: String) {
    val list by vm.health.collectAsState()
    val h = list.find { it.retailer.id == id }
    val term = h?.retailer?.termDays ?: 15

    val title = when (kind) {
        "payment" -> "Record payment"
        "promise" -> "Record promise"
        else -> "Record sale"
    }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var promiseDays by remember { mutableStateOf(7) }
    val amt = amount.toDoubleOrNull() ?: 0.0

    FormScaffold(title, nav) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (h != null) {
                Text(h.retailer.shopName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Outstanding: ${money(h.risk.outstanding)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
            }
            Field("Amount (₹)", amount, { amount = it.filter(Char::isDigit) }, KeyboardType.Number)

            if (kind == "sale") {
                Text(
                    "Due in $term days from today.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (kind == "promise") {
                Text("Promised to pay in", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 7, 15).forEach { d ->
                        FilterChip(selected = promiseDays == d, onClick = { promiseDays = d }, label = { Text("$d days") })
                    }
                }
            }
            Field("Note (optional)", note, { note = it })
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    when (kind) {
                        "payment" -> vm.addPayment(id, amt, note.ifBlank { null })
                        "promise" -> vm.addPromise(id, amt, promiseDays, note.ifBlank { null })
                        else -> vm.addSale(id, amt, term, note.ifBlank { null })
                    }
                    nav.popBackStack()
                },
                enabled = amt > 0,
                modifier = Modifier.fillMaxWidth(),
                colors = if (kind == "payment") ButtonDefaults.buttonColors(containerColor = SafeGreen) else ButtonDefaults.buttonColors()
            ) { Text("Save") }
        }
    }
}

@Composable
private fun FormScaffold(title: String, nav: NavController, content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { pad -> content(pad) }
}

@Composable
private fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboard: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        modifier = Modifier.fillMaxWidth()
    )
}
