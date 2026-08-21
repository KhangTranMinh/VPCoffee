package com.vpcoffee.feature.notifications.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import com.vpcoffee.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Locale

class NotificationSpeechService : Service(), TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "SpeechService"
        private const val CHANNEL_ID = "vpcoffee_speech_channel"
        private const val NOTIFICATION_ID = 1001

        private val _speechFlow = MutableSharedFlow<String>(extraBufferCapacity = 10)
        val speechFlow = _speechFlow.asSharedFlow()

        fun speak(context: Context, text: String) {
            if (text.isBlank()) return
            Log.d(TAG, "Queuing speech: $text")
            _speechFlow.tryEmit(text)
        }

        fun start(context: Context) {
            val intent = Intent(context, NotificationSpeechService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, NotificationSpeechService::class.java))
        }
    }

    private var textToSpeech: TextToSpeech? = null
    private var isReady = false
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        textToSpeech = TextToSpeech(applicationContext, this)

        serviceScope.launch {
            speechFlow.collect { text ->
                Log.d(TAG, "Processing speech: $text")
                if (isReady) {
                    textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vpcoffee-notification")
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onInit(status: Int) {
        isReady = status == TextToSpeech.SUCCESS
        if (isReady) {
            textToSpeech?.language = Locale.getDefault()
            Log.d(TAG, "TTS initialized successfully")
        } else {
            Log.e(TAG, "TTS initialization failed")
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isReady = false
        Log.d(TAG, "Service destroyed")
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Notification Speech",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps the app running to read notifications aloud"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.speech_service_notification_title))
            .setContentText(getString(R.string.speech_service_notification_text))
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
