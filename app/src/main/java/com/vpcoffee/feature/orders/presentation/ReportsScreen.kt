package com.vpcoffee.feature.orders.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vpcoffee.feature.orders.domain.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Grouping(val label: String, val pattern: String) {
    DAY("Day", "EEE, dd MMM yyyy"),
    WEEK("Week", "YYYY 'week' ww"),
    MONTH("Month", "MMMM yyyy"),
}

@Composable
fun ReportsScreen(viewModel: ReportsViewModel, contentPadding: PaddingValues) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    var grouping by remember { mutableStateOf(Grouping.DAY) }
    val totalIncome = orders.sumOf { it.total }
    val drinkSales = orders.flatMap { it.items }.groupBy { it.drinkName }.mapValues { (_, items) -> items.sumOf { it.quantity } }.toList().sortedByDescending { it.second }
    val groupedOrders = orders.groupBy { formatDate(it.createdAt, grouping.pattern) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, contentPadding.calculateTopPadding() + 16.dp, 16.dp, contentPadding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text("Total income", style = MaterialTheme.typography.titleLarge)
                    Text("$totalIncome đ", style = MaterialTheme.typography.displaySmall)
                    Text("${orders.size} completed orders", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        item {
            Text("Group orders by", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Grouping.entries.forEach { option -> TextButton(onClick = { grouping = option }) { Text(if (grouping == option) "• ${option.label}" else option.label) } }
            }
        }
        if (drinkSales.isNotEmpty()) {
            item { Text("Drink sales", style = MaterialTheme.typography.headlineSmall) }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        drinkSales.forEach { (name, quantity) -> Text("$name: $quantity sold", style = MaterialTheme.typography.titleMedium) }
                    }
                }
            }
        }
        if (groupedOrders.isEmpty()) {
            item { Text("No completed orders yet.", style = MaterialTheme.typography.titleLarge) }
        } else {
            groupedOrders.forEach { (label, group) ->
                item { Text(label, style = MaterialTheme.typography.headlineSmall) }
                items(group, key = { it.id }) { order -> OrderCard(order) }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order) = Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(formatDate(order.createdAt, "HH:mm"), style = MaterialTheme.typography.titleLarge)
        order.items.forEach { Text("${it.quantity} × ${it.drinkName}", style = MaterialTheme.typography.bodyLarge) }
        Text("Total: ${order.total} đ", style = MaterialTheme.typography.titleMedium)
    }
}

private fun formatDate(timestamp: Long, pattern: String): String = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
