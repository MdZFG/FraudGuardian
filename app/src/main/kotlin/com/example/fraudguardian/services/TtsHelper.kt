package com.example.fraudguardian.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import com.example.fraudguardian.R
import com.example.fraudguardian.util.PreferencesManager
import java.util.*

class TtsAlertService : Service(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var message: String? = null
    private val channelId = "fraudguardian_tts"
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    @Suppress("DEPRECATION")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        message = intent?.getStringExtra("message") ?: return START_NOT_STICKY

        createNotificationChannel()
        val notification = buildSilentNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                9991,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else startForeground(9991, notification)

        requestAudioFocus()
        tts = TextToSpeech(this, this)
        return START_NOT_STICKY
    }

    private fun buildSilentNotification(): Notification =
        NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("FraudGuardian Voice Alert")
            .setContentText("Speaking alert…")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .setOngoing(true)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(
                channelId,
                "FraudGuardian Voice Alerts",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background voice alerts"
                setSound(null, null)
                enableVibration(false)
                setShowBadge(false)
            }
            nm?.createNotificationChannel(ch)
        }
    }

    private fun requestAudioFocus() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attrs)
                .setAcceptsDelayedFocusGain(false)
                .build()
            audioManager?.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            stopSelf(); return
        }

        // Use PreferencesManager to get preferred language
        val prefs = PreferencesManager(this)
        val langCode = prefs.getPreferredLanguage()       // "en-IN" or "hi-IN"
        val locale = Locale.forLanguageTag(langCode)
        tts?.language = locale

        // Normalize numbers for digit-by-digit reading (Hindi digits untouched)
        val textToSpeak = normalizeMessageForSpeech(message ?: "")

        tts?.setOnUtteranceProgressListener(object :
            android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) = stopSafely()
            override fun onError(utteranceId: String?) = stopSafely()
        })

        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "FG_TTS")

        Handler(mainLooper).postDelayed({ stopSafely() }, 20000)
    }

    private fun stopSafely() {
        try {
            focusRequest?.let { audioManager?.abandonAudioFocusRequest(it) }
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun normalizeMessageForSpeech(text: String): String {
        // If message contains Hindi digits, leave as-is; else split English digits
        val numberRegex = Regex("\\d{4,}")
        return numberRegex.replace(text) { match ->
            if (match.value.any { it in '0'..'9' }) {
                match.value.toCharArray().joinToString(" ")
            } else match.value
        }
    }

    override fun onDestroy() {
        stopSafely()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
