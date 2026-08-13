package com.vpcoffee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vpcoffee.core.di.AppContainer
import com.vpcoffee.feature.catalog.presentation.CatalogScreen
import com.vpcoffee.feature.catalog.presentation.CatalogViewModel
import com.vpcoffee.feature.notifications.presentation.NotificationSettingsScreen
import com.vpcoffee.feature.orders.presentation.ReportsScreen
import com.vpcoffee.feature.orders.presentation.ReportsViewModel
import com.vpcoffee.feature.sales.presentation.PointOfSaleScreen
import com.vpcoffee.feature.sales.presentation.PointOfSaleViewModel
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
                val reportsViewModel: ReportsViewModel = viewModel(factory = ReportsViewModelFactory(appContainer))
                var selectedTab by rememberSaveable { mutableStateOf(AppTab.SALE) }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { TopAppBar(title = { Text(selectedTab.title) }) },
                    bottomBar = { AppNavigationBar(selectedTab) { selectedTab = it } },
                ) { padding ->
                    when (selectedTab) {
                        AppTab.SALE -> PointOfSaleScreen(pointOfSaleViewModel, padding)
                        AppTab.CATALOG -> CatalogScreen(catalogViewModel, padding)
                        AppTab.REPORTS -> ReportsScreen(reportsViewModel, padding)
                        AppTab.SETTINGS -> NotificationSettingsScreen(padding)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppNavigationBar(selectedTab: AppTab, onTabSelected: (AppTab) -> Unit) {
    Surface(shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AppTab.entries.forEach { tab ->
                FilterChip(
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    label = { Text(tab.navigationLabel) },
                )
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

private class ReportsViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ReportsViewModel(container.orderRepository) as T
}

private enum class AppTab(val title: String, val navigationLabel: String) {
    SALE("Point of Sale", "Sale"),
    CATALOG("Catalog & Inventory", "Catalog"),
    REPORTS("Orders & Reports", "Reports"),
    SETTINGS("Settings", "Settings"),
}

@Composable
fun VPCoffeeAppPreview() = VPCoffeeTheme { Text("VPCoffee") }
