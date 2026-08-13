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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vpcoffee.presentation.catalog.CatalogScreen
import com.vpcoffee.presentation.catalog.CatalogViewModel
import com.vpcoffee.presentation.pos.PointOfSaleScreen
import com.vpcoffee.presentation.pos.PointOfSaleViewModel
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
                val pointOfSaleViewModel: PointOfSaleViewModel = viewModel(factory = PointOfSaleViewModelFactory(appContainer))
                var selectedTab by rememberSaveable { mutableStateOf(AppTab.SALE) }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { TopAppBar(title = { Text(selectedTab.title) }) },
                    bottomBar = {
                        NavigationBar {
                            AppTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = selectedTab == tab,
                                    onClick = { selectedTab = tab },
                                    icon = { Text(tab.shortTitle) },
                                    label = { Text(tab.title) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    when (selectedTab) {
                        AppTab.SALE -> PointOfSaleScreen(pointOfSaleViewModel, padding)
                        AppTab.CATALOG -> CatalogScreen(catalogViewModel, padding)
                    }
                }
            }
        }
    }
}

private class CatalogViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CatalogViewModel(container.drinkRepository) as T
}

private class PointOfSaleViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PointOfSaleViewModel(
        container.drinkRepository,
        container.orderRepository,
    ) as T
}

private enum class AppTab(val title: String, val shortTitle: String) {
    SALE("Point of Sale", "Sale"),
    CATALOG("Catalog & Inventory", "Catalog"),
}

@Composable
fun VPCoffeeAppPreview() = VPCoffeeTheme { Text("VPCoffee") }
