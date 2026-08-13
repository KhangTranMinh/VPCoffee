package com.vpcoffee.presentation.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun NotificationSettingsScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().padding(
            start = 24.dp,
            end = 24.dp,
            top = contentPadding.calculateTopPadding() + 24.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text("Notification speech", style = MaterialTheme.typography.headlineMedium)
        Text("VPCoffee can read MoMo notifications aloud. Turn on notification access in Android settings to enable it.", style = MaterialTheme.typography.bodyLarge)
        Text("Speech can work while the screen is locked, but battery settings, audio use, and device restrictions may stop background speech.", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }) {
            Text("Open notification access settings")
        }
    }
}
