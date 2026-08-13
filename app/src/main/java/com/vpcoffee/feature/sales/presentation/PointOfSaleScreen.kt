package com.vpcoffee.feature.sales.presentation

import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vpcoffee.R
import com.vpcoffee.core.ui.formatVnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.orders.domain.model.OrderItem

@Composable
fun PointOfSaleScreen(viewModel: PointOfSaleViewModel, contentPadding: PaddingValues) {
    val drinks by viewModel.drinks.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    var showCart by remember { mutableStateOf(false) }
    var completedMessage by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = contentPadding.calculateBottomPadding()),
    ) {
        if (drinks.isEmpty()) {
            Text(stringResource(R.string.sale_add_drinks_first), modifier = Modifier.align(Alignment.Center).padding(32.dp), style = MaterialTheme.typography.titleLarge)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp, contentPadding.calculateTopPadding() + 16.dp, 16.dp, contentPadding.calculateBottomPadding() + 88.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) { items(drinks, key = { it.id }) { drink -> DrinkCard(drink) { viewModel.addDrink(drink) } } }
        }
        if (cart.isNotEmpty()) {
            Button(onClick = { showCart = true }, modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp).height(64.dp)) {
                Text(stringResource(R.string.sale_view_cart, cart.sumOf { it.quantity }, formatVnd(cart.sumOf { it.unitPrice * it.quantity })), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
    if (showCart) CartDialog(cart, drinks, { id, quantity -> viewModel.changeQuantity(id, quantity) }, onDismiss = { showCart = false }) {
        viewModel.completeOrder { showCart = false; completedMessage = true }
    }
    if (completedMessage) AlertDialog(onDismissRequest = { completedMessage = false }, title = { Text(stringResource(R.string.sale_order_saved)) }, text = { Text(stringResource(R.string.sale_order_saved_message)) }, confirmButton = { TextButton(onClick = { completedMessage = false }) { Text(stringResource(R.string.action_ok)) } })
}

@Composable
private fun DrinkCard(drink: Drink, onClick: () -> Unit) = Card(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DrinkImage(drink.imageUri, Modifier.fillMaxWidth().aspectRatio(1f), onClick)
        Text(drink.name, style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(formatVnd(drink.price), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Icon(
                imageVector = Icons.Default.AddShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

@Composable
private fun DrinkImage(uri: String?, modifier: Modifier, onClick: (() -> Unit)? = null) {
    if (uri == null) {
        Box(if (onClick == null) modifier else modifier.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Image, contentDescription = stringResource(R.string.sale_drink_image), modifier = Modifier.size(48.dp))
        }
    } else {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    onClick?.let { clickListener -> setOnClickListener { clickListener() } }
                }
            },
            update = { it.setImageURI(android.net.Uri.parse(uri)) },
        )
    }
}

@Composable
private fun CartDialog(
    items: List<OrderItem>,
    drinks: List<Drink>,
    onQuantityChange: (String, Int) -> Unit,
    onDismiss: () -> Unit,
    onDone: () -> Unit,
) = Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
) {
    val drinksById = drinks.associateBy { it.id }
    val total = items.sumOf { it.unitPrice * it.quantity }
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = AlertDialogDefaults.shape,
        tonalElevation = AlertDialogDefaults.TonalElevation,
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    stringResource(R.string.label_total, formatVnd(total)),
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                lazyItems(items, key = { it.drinkId }) { item ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    DrinkImage(drinksById[item.drinkId]?.imageUri, Modifier.size(64.dp))
                    Column(Modifier.weight(1f).padding(start = 16.dp)) {
                        Text(item.drinkName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            formatVnd(item.unitPrice * item.quantity),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = { onQuantityChange(item.drinkId, item.quantity - 1) }) {
                        Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.sale_decrease_quantity), modifier = Modifier.size(28.dp))
                    }
                    Text("${item.quantity}", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { onQuantityChange(item.drinkId, item.quantity + 1) }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.sale_increase_quantity), modifier = Modifier.size(28.dp))
                    }
                }
                }
            }
            Row(Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                TextButton(onClick = onDismiss, modifier = Modifier.height(56.dp)) { Text(stringResource(R.string.sale_keep_shopping), style = MaterialTheme.typography.titleMedium) }
                Button(onClick = onDone, modifier = Modifier.height(56.dp)) { Text(stringResource(R.string.sale_done), style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}
