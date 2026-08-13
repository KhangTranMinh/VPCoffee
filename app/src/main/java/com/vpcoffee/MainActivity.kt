package com.vpcoffee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vpcoffee.presentation.catalog.CatalogScreen
import com.vpcoffee.presentation.catalog.CatalogViewModel
import com.vpcoffee.ui.theme.VPCoffeeTheme

class MainActivity : ComponentActivity() {
    private val appContainer by lazy { AppContainer(applicationContext) }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VPCoffeeTheme {
                val catalogViewModel: CatalogViewModel = viewModel(factory = CatalogViewModelFactory(appContainer))
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { TopAppBar(title = { Text("Catalog & Inventory") }) },
                ) { padding -> CatalogScreen(catalogViewModel, padding) }
            }
        }
    }
}

private class CatalogViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CatalogViewModel(container.drinkRepository) as T
}

@Composable
fun VPCoffeeAppPreview() = VPCoffeeTheme { Text("VPCoffee") }
