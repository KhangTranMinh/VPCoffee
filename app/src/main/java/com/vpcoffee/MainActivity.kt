package com.vpcoffee

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vpcoffee.core.di.AppContainer
import com.vpcoffee.feature.catalog.presentation.CatalogScreen
import com.vpcoffee.feature.catalog.presentation.CatalogViewModel
import com.vpcoffee.feature.debug.presentation.DebugLogScreen
import com.vpcoffee.feature.debug.presentation.DebugLogViewModel
import com.vpcoffee.feature.settings.presentation.SettingsScreen
import com.vpcoffee.feature.orders.presentation.ReportsScreen
import com.vpcoffee.feature.orders.presentation.ReportsViewModel
import com.vpcoffee.feature.sales.presentation.PointOfSaleScreen
import com.vpcoffee.feature.sales.presentation.PointOfSaleViewModel
import com.vpcoffee.ui.theme.VPCoffeeTheme
import kotlin.math.sqrt

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
                var showDebugLog by remember { mutableStateOf(false) }

                ShakeDetector { showDebugLog = true }

                if (showDebugLog) {
                    val debugLogViewModel: DebugLogViewModel = viewModel()
                    DebugLogScreen(debugLogViewModel) { showDebugLog = false }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = { TopAppBar(title = { Text(stringResource(selectedTab.titleRes)) }) },
                        bottomBar = { AppNavigationBar(selectedTab) { selectedTab = it } },
                    ) { padding ->
                        when (selectedTab) {
                            AppTab.SALE -> PointOfSaleScreen(pointOfSaleViewModel, padding)
                            AppTab.CATALOG -> CatalogScreen(catalogViewModel, padding)
                            AppTab.REPORTS -> ReportsScreen(reportsViewModel, padding)
                            AppTab.SETTINGS -> SettingsScreen(padding)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShakeDetector(onShake: () -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(SensorManager::class.java) }
    val accelerometer = remember { sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            private var lastShakeTime = 0L
            private var shakeCount = 0
            private var lastShakeDetectedTime = 0L
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
                val now = System.currentTimeMillis()
                if (magnitude > 25.0) {
                    if (now - lastShakeDetectedTime < 500) {
                        shakeCount++
                    } else {
                        shakeCount = 1
                    }
                    lastShakeDetectedTime = now
                    if (shakeCount >= 3 && now - lastShakeTime > 2000) {
                        lastShakeTime = now
                        shakeCount = 0
                        onShake()
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager?.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager?.unregisterListener(listener) }
    }
}

@Composable
private fun AppNavigationBar(selectedTab: AppTab, onTabSelected: (AppTab) -> Unit) {
    NavigationBar(modifier = Modifier.height(96.dp)) {
        AppTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(tab.icon, contentDescription = stringResource(tab.titleRes)) },
                label = { Text(stringResource(tab.navigationLabelRes), style = androidx.compose.material3.MaterialTheme.typography.titleSmall) },
            )
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

private enum class AppTab(
    @StringRes val titleRes: Int,
    @StringRes val navigationLabelRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    SALE(R.string.tab_point_of_sale, R.string.tab_sale, Icons.Default.ShoppingCart),
    CATALOG(R.string.tab_catalog_inventory, R.string.tab_catalog, Icons.Default.Storefront),
    REPORTS(R.string.tab_orders_reports, R.string.tab_reports, Icons.Default.ReceiptLong),
    SETTINGS(R.string.tab_settings, R.string.tab_settings, Icons.Default.Settings),
}

@Composable
fun VPCoffeeAppPreview() = VPCoffeeTheme { Text(stringResource(R.string.app_name)) }
