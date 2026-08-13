package com.vpcoffee.feature.catalog.presentation

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialogDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.content.FileProvider
import com.vpcoffee.R
import com.vpcoffee.core.ui.formatVnd
import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.catalog.data.image.cropImageToSquare
import java.io.File
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun CatalogScreen(viewModel: CatalogViewModel, contentPadding: PaddingValues) {
    val drinks by viewModel.drinks.collectAsStateWithLifecycle()
    var editingDrink by remember { mutableStateOf<Drink?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = contentPadding.calculateBottomPadding()),
    ) {
        if (drinks.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.catalog_no_drinks), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.catalog_add_first_drink), style = MaterialTheme.typography.bodyLarge)
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
        ) { Text(stringResource(R.string.action_add)) }
    }

    if (showAddDialog) DrinkEditorDialog(onDismiss = { showAddDialog = false }) { name, price, image ->
        viewModel.saveDrink(null, name, price, image)
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
                Text(formatVnd(drink.price), style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = onDelete) { Text(stringResource(R.string.action_delete)) }
        }
    }
}

@Composable
private fun DrinkEditorDialog(drink: Drink? = null, onDismiss: () -> Unit, onSave: (String, String, String?) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var name by remember(drink) { mutableStateOf(drink?.name.orEmpty()) }
    var price by remember(drink) { mutableStateOf(drink?.price?.toString().orEmpty()) }
    var imageUri by remember(drink) { mutableStateOf(drink?.imageUri) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> imageUri = uri?.toString() }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isSaved ->
        cameraImageUri?.let { uri ->
            if (isSaved) {
                coroutineScope.launch {
                    if (cropImageToSquare(context, uri)) imageUri = uri.toString()
                }
            }
        }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(if (drink == null) R.string.catalog_add_drink else R.string.catalog_edit_drink), style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.catalog_drink_name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.catalog_price_vnd)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = { imagePicker.launch("image/*") }) { Text(stringResource(R.string.catalog_choose_square_image)) }
                Button(onClick = {
                    createCameraImageUri(context).also { uri ->
                        cameraImageUri = uri
                        cameraLauncher.launch(uri)
                    }
                }) { Text(stringResource(R.string.catalog_take_photo)) }
                imageUri?.let { DrinkImage(it, Modifier.size(88.dp)) }
                Row(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
                    TextButton(onClick = { onSave(name, price, imageUri) }) { Text(stringResource(R.string.action_save)) }
                }
            }
        }
    }
}

private fun createCameraImageUri(context: Context): Uri {
    val imageDirectory = File(context.filesDir, "images").apply { mkdirs() }
    val imageFile = File(imageDirectory, "drink-${UUID.randomUUID()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
}

@Composable
private fun DrinkImage(uri: String?, modifier: Modifier = Modifier) {
    if (uri == null) {
        Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) { Text(stringResource(R.string.catalog_no_image)) }
    } else {
        AndroidView(
            modifier = modifier.aspectRatio(1f),
            factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
            update = { it.setImageURI(android.net.Uri.parse(uri)) },
        )
    }
}
