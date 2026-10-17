package com.premsingh.pantrypal

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.premsingh.pantrypal.data.PantryEntry
import com.premsingh.pantrypal.data.PantryViewModel
import com.premsingh.pantrypal.domain.daysLabel
import com.premsingh.pantrypal.domain.summarizeExpiry

private val Leaf = Color(0xFF315D48)
private val Canvas = Color(0xFFF4F6F0)
private val Ink = Color(0xFF202820)
private val Muted = Color(0xFF707B70)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { PantryPalApp() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantryPalApp(vm: PantryViewModel = viewModel()) {
    val entries by vm.entries.collectAsStateWithLifecycle()
    val today = System.currentTimeMillis() / 86_400_000L
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAdd by remember { mutableStateOf(false) }
    val categories = listOf("All", "Produce", "Dairy", "Pantry", "Other")
    val summary = summarizeExpiry(entries.map { it.expiryEpochDay }, today)
    val visible = entries.filter { it.name.contains(search, ignoreCase = true) }
        .filter { selectedCategory == "All" || it.category == selectedCategory }

    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = Leaf, background = Canvas, surface = Color.White, onBackground = Ink)) {
        Scaffold(containerColor = Canvas, topBar = { TopAppBar(title = { Text("PantryPal", fontWeight = FontWeight.Bold) }) },
            floatingActionButton = { Button(onClick = { showAdd = true }) { Text("＋ Add item") } }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("A little less waste, one shelf at a time.", color = Muted, modifier = Modifier.padding(top = 4.dp))
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SummaryCard("In your pantry", summary.itemCount.toString(), Modifier.weight(1f))
                        SummaryCard("Use soon", summary.expiringSoonCount.toString(), Modifier.weight(1f), Color(0xFFFFF0D7))
                        SummaryCard("Expired", summary.expiredCount.toString(), Modifier.weight(1f), Color(0xFFFBE5E0))
                    }
                }
                item { OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), label = { Text("Search your pantry") }, singleLine = true) }
                item { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { categories.forEach { category ->
                    FilterChip(selectedCategory == category, onClick = { selectedCategory = category }, label = { Text(category) })
                } } }
                if (visible.isEmpty()) item { Text("Your pantry is clear. Add an item to get started.", color = Muted, modifier = Modifier.padding(vertical = 28.dp)) }
                items(visible, key = { it.id }) { entry -> PantryRow(entry, today) { vm.markUsed(entry.id) } }
                item { Text("Saved on this device · no account or cloud sync", color = Muted, modifier = Modifier.padding(vertical = 16.dp)) }
            }
        }
        if (showAdd) AddItemDialog(onDismiss = { showAdd = false }, onAdd = { name, category, quantity, days ->
            vm.add(name, category, quantity, days); showAdd = false
        })
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier, color: Color = Color.White) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.padding(13.dp)) { Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Leaf); Text(title, style = MaterialTheme.typography.labelSmall, color = Muted) }
    }
}

@Composable
private fun PantryRow(entry: PantryEntry, today: Long, onUsed: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(entry.name, fontWeight = FontWeight.SemiBold, color = Ink)
                Text("${entry.quantity} · ${entry.category}", color = Muted, style = MaterialTheme.typography.bodySmall)
                Text(daysLabel(entry.expiryEpochDay, today), color = if (entry.expiryEpochDay - today <= 3) Color(0xFF9B5A17) else Leaf, style = MaterialTheme.typography.labelMedium)
            }
            TextButton(onClick = onUsed) { Text("Used") }
        }
    }
}

@Composable
private fun AddItemDialog(onDismiss: () -> Unit, onAdd: (String, String, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }; var quantity by remember { mutableStateOf("1") }; var days by remember { mutableStateOf("3") }
    var category by remember { mutableStateOf("Produce") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add pantry item") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Item name") }, singleLine = true)
            OutlinedTextField(quantity, { quantity = it }, label = { Text("Quantity") }, singleLine = true)
            OutlinedTextField(days, { days = it.filter(Char::isDigit).take(3) }, label = { Text("Days until expiry") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { listOf("Produce", "Dairy", "Pantry", "Other").forEach {
                FilterChip(category == it, onClick = { category = it }, label = { Text(it) })
            } }
        }
    }, confirmButton = { TextButton(enabled = name.isNotBlank() && (days.toIntOrNull() ?: -1) >= 0, onClick = { onAdd(name, category, quantity, days.toIntOrNull() ?: 0) }) { Text("Save item") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}
