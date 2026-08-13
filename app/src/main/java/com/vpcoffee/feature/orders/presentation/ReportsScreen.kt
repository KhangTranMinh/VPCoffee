package com.vpcoffee.feature.orders.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.annotation.StringRes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import com.vpcoffee.R
import com.vpcoffee.core.ui.formatVnd
import com.vpcoffee.feature.orders.domain.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Grouping(@StringRes val labelRes: Int, @StringRes val patternRes: Int) {
    DAY(R.string.report_day, R.string.date_format_day),
    WEEK(R.string.report_week, R.string.date_format_week),
    MONTH(R.string.report_month, R.string.date_format_month),
}

@Composable
fun ReportsScreen(viewModel: ReportsViewModel, contentPadding: PaddingValues) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    var grouping by remember { mutableStateOf(Grouping.DAY) }
    val totalIncome = orders.sumOf { it.total }
    val drinkSales = orders.flatMap { it.items }.groupBy { it.drinkName }.mapValues { (_, items) -> items.sumOf { it.quantity } }.toList().sortedByDescending { it.second }
    val groupedOrders = orders.groupBy { formatDate(it.createdAt, stringResource(grouping.patternRes)) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp, contentPadding.calculateTopPadding() + 16.dp, 16.dp, contentPadding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            TabRow(selectedTabIndex = Grouping.entries.indexOf(grouping)) {
                Grouping.entries.forEach { option ->
                    Tab(
                        selected = grouping == option,
                        onClick = { grouping = option },
                        text = {
                            Text(
                                stringResource(option.labelRes),
                                style = MaterialTheme.typography.titleLarge,
                            )
                        },
                    )
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.report_total_income), style = MaterialTheme.typography.titleLarge)
                    Text(formatVnd(totalIncome), style = MaterialTheme.typography.displaySmall)
                    Text(pluralStringResource(R.plurals.report_completed_orders, orders.size, orders.size), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        if (drinkSales.isNotEmpty()) {
            item { Text(stringResource(R.string.report_drink_sales), style = MaterialTheme.typography.headlineSmall) }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        drinkSales.forEach { (name, quantity) -> Text(stringResource(R.string.report_drink_sales_item, name, quantity), style = MaterialTheme.typography.titleMedium) }
                    }
                }
            }
        }
        if (groupedOrders.isEmpty()) {
            item { Text(stringResource(R.string.report_no_completed_orders), style = MaterialTheme.typography.titleLarge) }
        } else {
            groupedOrders.forEach { (label, group) ->
                item { Text(label, style = MaterialTheme.typography.headlineSmall) }
                items(group, key = { it.id }) { order -> OrderCard(order) { selectedOrder = order } }
            }
        }
    }
    selectedOrder?.let { order -> OrderDetailsDialog(order, onDismiss = { selectedOrder = null }) }
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit) = Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(formatDate(order.createdAt, stringResource(R.string.date_format_time)), style = MaterialTheme.typography.titleLarge)
        val itemCount = order.items.sumOf { it.quantity }
        Text(pluralStringResource(R.plurals.report_order_items, itemCount, itemCount), style = MaterialTheme.typography.bodyLarge)
        Text(stringResource(R.string.label_total, formatVnd(order.total)), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun OrderDetailsDialog(order: Order, onDismiss: () -> Unit) = Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = AlertDialogDefaults.shape,
        tonalElevation = AlertDialogDefaults.TonalElevation,
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.report_order_details), style = MaterialTheme.typography.displaySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    formatDate(order.createdAt, stringResource(R.string.date_format_order_details)),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(order.items, key = { it.drinkId }) { item ->
                    Text(
                        stringResource(R.string.report_order_item, item.quantity, item.drinkName),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            Text(
                stringResource(R.string.label_total, formatVnd(order.total)),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(64.dp)) {
                Text(stringResource(R.string.action_ok), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun formatDate(timestamp: Long, pattern: String): String = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
