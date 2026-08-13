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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.annotation.StringRes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vpcoffee.R
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

    LazyColumn(
        contentPadding = PaddingValues(16.dp, contentPadding.calculateTopPadding() + 16.dp, 16.dp, contentPadding.calculateBottomPadding() + 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.report_total_income), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.currency_vnd, totalIncome), style = MaterialTheme.typography.displaySmall)
                    Text(pluralStringResource(R.plurals.report_completed_orders, orders.size, orders.size), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        item {
            Text(stringResource(R.string.report_group_orders_by), style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Grouping.entries.forEach { option ->
                    val label = stringResource(option.labelRes)
                    TextButton(onClick = { grouping = option }) { Text(if (grouping == option) stringResource(R.string.report_selected_grouping, label) else label) }
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
                items(group, key = { it.id }) { order -> OrderCard(order) }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order) = Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(formatDate(order.createdAt, stringResource(R.string.date_format_time)), style = MaterialTheme.typography.titleLarge)
        order.items.forEach { Text(stringResource(R.string.report_order_item, it.quantity, it.drinkName), style = MaterialTheme.typography.bodyLarge) }
        Text(stringResource(R.string.label_total, stringResource(R.string.currency_vnd, order.total)), style = MaterialTheme.typography.titleMedium)
    }
}

private fun formatDate(timestamp: Long, pattern: String): String = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
