package com.vpcoffee.feature.orders.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vpcoffee.R
import com.vpcoffee.core.ui.formatVnd
import com.vpcoffee.feature.orders.domain.model.Order
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class ReportPeriod(@StringRes val labelRes: Int) {
    DAY(R.string.report_day),
    WEEK(R.string.report_week),
    MONTH(R.string.report_month),
}

@Composable
fun ReportsScreen(viewModel: ReportsViewModel, contentPadding: PaddingValues, onRegisterSend: ((() -> Unit) -> Unit)? = null) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    var period by remember { mutableStateOf(ReportPeriod.DAY) }
    val periodOrders = orders.filter { it.isInPeriod(period) }
    val totalIncome = periodOrders.sumOf { it.total }
    val drinkSales = periodOrders.flatMap { it.items }.groupBy { it.drinkName }
        .mapValues { (_, items) -> items.sumOf { it.quantity } }.toList()
        .sortedByDescending { it.second }
    val groupedOrders =
        periodOrders.groupBy { formatDate(it.createdAt, stringResource(R.string.date_format_day)) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var showDrinkSales by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Register send callback
    LaunchedEffect(periodOrders) {
        onRegisterSend?.invoke {
            if (periodOrders.isEmpty()) {
                android.widget.Toast.makeText(context, context.getString(R.string.report_no_completed_orders), android.widget.Toast.LENGTH_SHORT).show()
                return@invoke
            }
            val unsentOrders = periodOrders.filter { !it.isSent }
            if (unsentOrders.isEmpty()) {
                android.widget.Toast.makeText(context, "All orders already sent", android.widget.Toast.LENGTH_SHORT).show()
                return@invoke
            }
            isSending = true
            val defaultEmail = "tran.minhkhang.1989.tester@gmail.com"
            val email = context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
                .getString("email", defaultEmail) ?: defaultEmail
            viewModel.exportAndSendCsv(context, unsentOrders, email) {
                isSending = false
            }
        }
    }

    if (isSending) {
        Dialog(onDismissRequest = {}) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = AlertDialogDefaults.shape,
                tonalElevation = AlertDialogDefaults.TonalElevation,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                    Text("Sending orders...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(
            16.dp,
            contentPadding.calculateTopPadding() + 16.dp,
            16.dp,
            contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            TabRow(selectedTabIndex = ReportPeriod.entries.indexOf(period)) {
                ReportPeriod.entries.forEach { option ->
                    Tab(
                        selected = period == option,
                        onClick = { period = option },
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
            Card(
                Modifier
                    .fillMaxWidth()
                    .clickable { showDrinkSales = true }
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.report_total_income),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            pluralStringResource(
                                R.plurals.report_completed_orders,
                                periodOrders.size,
                                periodOrders.size
                            ),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Text(formatVnd(totalIncome), style = MaterialTheme.typography.displaySmall)
                }
            }
        }
        if (groupedOrders.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.report_no_completed_orders),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        } else {
            groupedOrders.forEach { (label, group) ->
                item { Text(label, style = MaterialTheme.typography.headlineSmall) }
                items(group, key = { it.id }) { order ->
                    OrderCard(order) {
                        selectedOrder = order
                    }
                }
            }
        }
    }
    selectedOrder?.let { order -> OrderDetailsDialog(order, onDismiss = { selectedOrder = null }) }
    if (showDrinkSales) {
        DrinkSalesDialog(
            drinkSales,
            period = period,
            onDismiss = { showDrinkSales = false },
        )
    }
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit) = Card(
    Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        val itemCount = order.items.sumOf { it.quantity }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(6.dp))
            Text(
                formatDate(order.createdAt, stringResource(R.string.date_format_time)),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.weight(1f))
            Text(
                pluralStringResource(R.plurals.report_order_items, itemCount, itemCount),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            stringResource(R.string.label_total, formatVnd(order.total)),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun OrderDetailsDialog(order: Order, onDismiss: () -> Unit) = Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = AlertDialogDefaults.shape,
        tonalElevation = AlertDialogDefaults.TonalElevation,
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                stringResource(R.string.label_total, formatVnd(order.total)),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    formatDate(order.createdAt, stringResource(R.string.date_format_day)) + " - " +
                        formatDate(order.createdAt, stringResource(R.string.date_format_time)),
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
            Button(
                onClick = onDismiss, modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text(
                    stringResource(R.string.action_ok),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun DrinkSalesDialog(
    drinkSales: List<Pair<String, Int>>,
    period: ReportPeriod,
    onDismiss: () -> Unit,
) = Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = AlertDialogDefaults.shape,
        tonalElevation = AlertDialogDefaults.TonalElevation,
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                stringResource(R.string.report_drink_sales),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    formatPeriodDate(period),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(drinkSales) { (name, quantity) ->
                    Text(
                        stringResource(R.string.report_order_item, quantity, name),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            Button(
                onClick = onDismiss, modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Text(
                    stringResource(R.string.action_ok),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long, pattern: String): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))

private fun formatPeriodDate(period: ReportPeriod): String {
    val cal = Calendar.getInstance()
    return when (period) {
        ReportPeriod.DAY -> formatDate(cal.timeInMillis, "EEE, dd MMM yyyy")
        ReportPeriod.WEEK -> {
            val start = cal.clone() as Calendar
            start.set(Calendar.DAY_OF_WEEK, start.firstDayOfWeek)
            val end = cal.clone() as Calendar
            end.set(Calendar.DAY_OF_WEEK, end.firstDayOfWeek)
            end.add(Calendar.DAY_OF_WEEK, 6)
            "${formatDate(start.timeInMillis, "dd MMM")} - ${formatDate(end.timeInMillis, "dd MMM yyyy")}"
        }
        ReportPeriod.MONTH -> formatDate(cal.timeInMillis, "MMM yyyy")
    }
}

private fun Order.isInPeriod(period: ReportPeriod): Boolean {
    val now = Calendar.getInstance()
    val orderDate = Calendar.getInstance().apply { timeInMillis = createdAt }
    return when (period) {
        ReportPeriod.DAY -> orderDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                orderDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

        ReportPeriod.WEEK -> orderDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                orderDate.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)

        ReportPeriod.MONTH -> orderDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                orderDate.get(Calendar.MONTH) == now.get(Calendar.MONTH)
    }
}
