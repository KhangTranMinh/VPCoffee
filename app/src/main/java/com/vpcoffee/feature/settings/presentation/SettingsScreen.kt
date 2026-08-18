package com.vpcoffee.feature.settings.presentation

import android.content.Intent
import android.content.ComponentName
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.vpcoffee.R

@Composable
fun SettingsScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var notificationAccessGranted by androidx.compose.runtime.remember {
        mutableStateOf(hasNotificationAccess(context))
    }
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationAccessGranted = hasNotificationAccess(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(
            start = 24.dp,
            end = 24.dp,
            top = contentPadding.calculateTopPadding() + 24.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(stringResource(R.string.settings_notification_speech_title), style = MaterialTheme.typography.headlineMedium)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (notificationAccessGranted) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                },
            ),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(
                        if (notificationAccessGranted) R.string.settings_notification_access_granted
                        else R.string.settings_notification_access_not_granted,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    stringResource(
                        if (notificationAccessGranted) R.string.settings_notification_access_granted_description
                        else R.string.settings_notification_access_not_granted_description,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        if (!notificationAccessGranted) {
            Text(stringResource(R.string.settings_notification_access_instruction), style = MaterialTheme.typography.bodyLarge)
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                modifier = Modifier.height(64.dp),
            ) {
                Text(stringResource(R.string.settings_grant_access), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun hasNotificationAccess(context: android.content.Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners",
    ).orEmpty()
    val serviceName = ComponentName(
        context,
        "com.vpcoffee.feature.notifications.data.VPCoffeeNotificationListenerService",
    ).flattenToString()
    return enabledListeners.split(':').any { it == serviceName }
}
