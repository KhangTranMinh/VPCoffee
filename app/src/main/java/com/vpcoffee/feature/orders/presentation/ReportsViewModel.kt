package com.vpcoffee.feature.orders.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.catalog.domain.repository.DrinkRepository
import com.vpcoffee.feature.orders.domain.model.Order
import com.vpcoffee.feature.orders.domain.model.OrderItem
import com.vpcoffee.feature.orders.domain.repository.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportsViewModel(
    private val orderRepository: OrderRepository,
    drinkRepository: DrinkRepository,
) : ViewModel() {

    private val drinks: StateFlow<List<Drink>> = drinkRepository.observeDrinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val orders: StateFlow<List<Order>> = combine(
        orderRepository.observeOrders(),
        drinks,
    ) { orders, drinks ->
        val drinkNames = drinks.associate { it.id to it.name }
        orders.map { order ->
            order.copy(
                items = order.items.map { item ->
                    item.copy(drinkName = drinkNames[item.drinkId] ?: item.drinkName)
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun exportAndSendCsv(context: Context, orders: List<Order>, email: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val csvContent = generateCsv(orders)
                val file = saveCsvToFile(context, csvContent)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                sendEmail(context, uri, email, orders.size)
                markOrdersAsSent(orders)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onComplete()
            }
        }
    }

    private fun generateCsv(orders: List<Order>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.appendLine("Order ID,Date,Time,Drink,Quantity,Unit Price,Total")
        orders.forEach { order ->
            val date = dateFormat.format(Date(order.createdAt))
            val time = timeFormat.format(Date(order.createdAt))
            order.items.forEach { item ->
                sb.appendLine("${order.id},$date,$time,${item.drinkName},${item.quantity},${item.unitPrice},${item.unitPrice * item.quantity}")
            }
        }
        return sb.toString()
    }

    private fun saveCsvToFile(context: Context, csvContent: String): File {
        val dir = File(context.cacheDir, "exports")
        dir.mkdirs()
        val file = File(dir, "orders_${System.currentTimeMillis()}.csv")
        file.writeText(csvContent)
        return file
    }

    private fun sendEmail(context: Context, uri: Uri, email: String, orderCount: Int) {
        val gmailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            setPackage("com.google.android.gm")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, "VPCoffee Orders Report - $orderCount orders")
            putExtra(Intent.EXTRA_TEXT, "Please find attached the orders report.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (gmailIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(gmailIntent)
        } else {
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "VPCoffee Orders Report - $orderCount orders")
                putExtra(Intent.EXTRA_TEXT, "Please find attached the orders report.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Send email"))
        }
    }

    private suspend fun markOrdersAsSent(orders: List<Order>) {
        val orderIds = orders.map { it.id }
        orderRepository.markOrdersAsSent(orderIds)
    }
}
