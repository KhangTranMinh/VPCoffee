package com.vpcoffee.feature.catalog.presentation

import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vpcoffee.feature.catalog.domain.model.Drink

@Composable
fun CatalogScreen(viewModel: CatalogViewModel, contentPadding: PaddingValues) {
    val drinks by viewModel.drinks.collectAsStateWithLifecycle()
    var editingDrink by remember { mutableStateOf<Drink?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (drinks.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No drinks yet", style = MaterialTheme.typography.headlineSmall)
                Text("Add your first drink to start taking orders.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = contentPadding.calculateTopPadding() + 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(drinks, key = { it.id }) { drink ->
                    DrinkRow(drink, onClick = { editingDrink = drink }, onDelete = { viewModel.deleteDrink(drink.id) })
                }
            }
        }
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        ) { Text("Add") }
    }

    if (showAddDialog) DrinkEditorDialog(onDismiss = { showAddDialog = false }) { name, price, image ->
        viewModel.saveDrink(0, name, price, image)
        showAddDialog = false
    }
    editingDrink?.let { drink ->
        DrinkEditorDialog(drink, onDismiss = { editingDrink = null }) { name, price, image ->
            viewModel.saveDrink(drink.id, name, price, image)
            editingDrink = null
        }
    }
}

@Composable
private fun DrinkRow(drink: Drink, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            DrinkImage(drink.imageUri, Modifier.size(76.dp))
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                Text(drink.name, style = MaterialTheme.typography.titleLarge)
                Text("${drink.price} đ", style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

@Composable
private fun DrinkEditorDialog(drink: Drink? = null, onDismiss: () -> Unit, onSave: (String, String, String?) -> Unit) {
    var name by remember(drink) { mutableStateOf(drink?.name.orEmpty()) }
    var price by remember(drink) { mutableStateOf(drink?.price?.toString().orEmpty()) }
    var imageUri by remember(drink) { mutableStateOf(drink?.imageUri) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> imageUri = uri?.toString() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (drink == null) "Add drink" else "Edit drink") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Drink name") }, singleLine = true)
                OutlinedTextField(price, { price = it }, label = { Text("Price (VND)") }, singleLine = true)
                Button(onClick = { imagePicker.launch("image/*") }) { Text("Choose square image") }
                imageUri?.let { DrinkImage(it, Modifier.size(88.dp)) }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, price, imageUri) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun DrinkImage(uri: String?, modifier: Modifier = Modifier) {
    if (uri == null) {
        Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) { Text("Photo") }
    } else {
        AndroidView(
            modifier = modifier.aspectRatio(1f),
            factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
            update = { it.setImageURI(android.net.Uri.parse(uri)) },
        )
    }
}
