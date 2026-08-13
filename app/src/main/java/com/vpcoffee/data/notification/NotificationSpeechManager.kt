package com.vpcoffee.data.notification

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object NotificationSpeechManager : TextToSpeech.OnInitListener {
    private var textToSpeech: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null

    fun speak(context: Context, text: String) {
        if (text.isBlank()) return
        val engine = textToSpeech
        if (engine == null) {
            pendingText = text
            textToSpeech = TextToSpeech(context.applicationContext, this)
        } else if (isReady) {
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vpcoffee-notification")
        } else {
            pendingText = text
        }
    }

    override fun onInit(status: Int) {
        isReady = status == TextToSpeech.SUCCESS
        if (isReady) {
            textToSpeech?.language = Locale.getDefault()
            pendingText?.let { textToSpeech?.speak(it, TextToSpeech.QUEUE_FLUSH, null, "vpcoffee-notification") }
            pendingText = null
        }
    }

    fun release() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isReady = false
    }
}
