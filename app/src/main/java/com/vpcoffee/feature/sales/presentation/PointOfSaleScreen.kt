package com.vpcoffee.feature.sales.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.orders.domain.model.OrderItem

@Composable
fun PointOfSaleScreen(viewModel: PointOfSaleViewModel, contentPadding: PaddingValues) {
    val drinks by viewModel.drinks.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    var showCart by remember { mutableStateOf(false) }
    var completedMessage by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        if (drinks.isEmpty()) {
            Text("Add drinks in Catalog before taking an order.", modifier = Modifier.align(Alignment.Center).padding(32.dp), style = MaterialTheme.typography.titleLarge)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                contentPadding = PaddingValues(16.dp, contentPadding.calculateTopPadding() + 16.dp, 16.dp, contentPadding.calculateBottomPadding() + 88.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) { items(drinks, key = { it.id }) { drink -> DrinkCard(drink) { viewModel.addDrink(drink) } } }
        }
        if (cart.isNotEmpty()) {
            Button(onClick = { showCart = true }, modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp)) {
                Text("View cart (${cart.sumOf { it.quantity }}) — ${cart.sumOf { it.unitPrice * it.quantity }} đ")
            }
        }
    }
    if (showCart) CartDialog(cart, { id, quantity -> viewModel.changeQuantity(id, quantity) }, onDismiss = { showCart = false }) {
        viewModel.completeOrder { showCart = false; completedMessage = true }
    }
    if (completedMessage) AlertDialog(onDismissRequest = { completedMessage = false }, title = { Text("Order saved") }, text = { Text("The completed order was saved successfully.") }, confirmButton = { TextButton(onClick = { completedMessage = false }) { Text("OK") } })
}

@Composable
private fun DrinkCard(drink: Drink, onClick: () -> Unit) = Card(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
) {
    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(drink.name, style = MaterialTheme.typography.titleLarge)
        Text("${drink.price} đ", style = MaterialTheme.typography.titleMedium)
        Text("Tap to add", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CartDialog(items: List<OrderItem>, onQuantityChange: (String, Int) -> Unit, onDismiss: () -> Unit, onDone: () -> Unit) = AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Your cart") },
    text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEach { item ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text(item.drinkName, style = MaterialTheme.typography.titleMedium); Text("${item.unitPrice * item.quantity} đ") }
                    TextButton(onClick = { onQuantityChange(item.drinkId, item.quantity - 1) }) { Text("−") }
                    Text("${item.quantity}", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = { onQuantityChange(item.drinkId, item.quantity + 1) }) { Text("+") }
                }
            }
            Text("Total: ${items.sumOf { it.unitPrice * it.quantity }} đ", style = MaterialTheme.typography.titleLarge)
        }
    },
    confirmButton = { Button(onClick = onDone) { Text("Done") } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Keep shopping") } },
)
