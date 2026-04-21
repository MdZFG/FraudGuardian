package com.example.fraudguardian.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsMessage
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fraudguardian.R
import com.example.fraudguardian.db.AppDatabase
import com.example.fraudguardian.entities.OtpLog
import com.example.fraudguardian.services.SosScheduler
import com.example.fraudguardian.services.TtsAlertService
import com.example.fraudguardian.util.PreferencesManager
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    private val spamKeywords = listOf(
        "win", "lottery", "loan", "payment", "offer", "free", "click", "link",
        "urgent", "cashback", "claim", "reward", "gift", "card blocked", "otp",
        "bank", "verify", "kyc", "pan", "account", "refund"
    )

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val bundle = intent.extras
            val pdus = bundle?.get("pdus") as? Array<*> ?: return
            val format = bundle.getString("format")
            val prefs = PreferencesManager(context)
            val isHindi = prefs.isHindiVoiceEnabled()

            pdus.forEach { pdu ->
                val sms = SmsMessage.createFromPdu(pdu as ByteArray, format)
                val from = sms.originatingAddress ?: "Unknown"
                val body = sms.messageBody ?: ""
                Log.d("SmsReceiver", "SMS from $from: $body")

                val code = extractOtp(body)
                val containsSpam = spamKeywords.any { body.contains(it, ignoreCase = true) }

                CoroutineScope(Dispatchers.IO).launch {
                    AppDatabase.getInstance(context).appDao().insertOtp(
                        OtpLog(
                            sender = from,
                            message = body,
                            code = code ?: if (containsSpam) "SPAM" else null,
                            receivedAt = Date().time
                        )
                    )
                }

                Handler(Looper.getMainLooper()).post {
                    val text = when {
                        code != null -> if (isHindi) "OTP प्राप्त हुआ $from से" else "OTP received from $from"
                        containsSpam -> if (isHindi) "संदिग्ध संदेश प्राप्त हुआ $from से" else "Suspicious message detected from $from"
                        else -> "Message received"
                    }
                    android.widget.Toast.makeText(context, text, android.widget.Toast.LENGTH_LONG).show()
                }

                when {
                    code != null -> {
                        val msg = if (isHindi) "सावधान! OTP प्राप्त हुआ $from से। कोड: $code" else "Warning! OTP received from $from. Code is $code"
                        startAlert(context, from, msg, isSpam = false)
                        SosScheduler.scheduleSos(context, "OTP from $from: $code")
                    }
                    containsSpam -> {
                        val msg = if (isHindi) "सावधान! संदिग्ध संदेश प्राप्त हुआ $from से।" else "Caution! Suspicious message detected from $from."
                        startAlert(context, from, msg, isSpam = true)
                        SosScheduler.scheduleSos(context, "Spam from $from: $body")
                    }
                }

                context.sendBroadcast(Intent("com.example.fraudguardian.UPDATE_LOGS"))
            }

        } catch (e: Exception) {
            Log.e("SmsReceiver", "Error processing SMS", e)
        }
    }

    private fun startAlert(context: Context, from: String, msg: String, isSpam: Boolean) {
        val ttsIntent = Intent(context, TtsAlertService::class.java)
        ttsIntent.putExtra("message", msg)
        try { context.startForegroundService(ttsIntent) } catch (e: Exception) { context.startService(ttsIntent) }

        val channelId = "fraud_alert_channel"
        val nm = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Fraud Alerts", NotificationManager.IMPORTANCE_HIGH)
            nm?.createNotificationChannel(ch)
        }

        val alertIntent = Intent(context, com.example.fraudguardian.ui.AlertActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("message", msg)
            putExtra("from", from)
            putExtra("isSpam", isSpam)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, alertIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notif = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Fraud Alert")
            .setContentText(msg)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        nm?.notify(System.currentTimeMillis().toInt(), notif)
    }

    private fun extractOtp(text: String): String? {
        val otpRegex = Regex("(?<!\\d)(\\d{4,6})(?!\\d)")
        val m = otpRegex.find(text)
        if (m != null) return m.groupValues[1]
        val keyRegex = Regex("(?i)\\b(otp|one time|verification code|code)\\b")
        if (keyRegex.containsMatchIn(text)) {
            val d = Regex("\\d{4,6}").find(text)
            if (d != null) return d.value
        }
        return null
    }
}
