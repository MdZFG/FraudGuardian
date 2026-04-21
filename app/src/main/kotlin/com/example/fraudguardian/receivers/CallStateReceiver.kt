package com.example.fraudguardian.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.Log
import com.example.fraudguardian.db.AppDatabase
import com.example.fraudguardian.entities.OtpLog
import com.example.fraudguardian.services.SosScheduler
import com.example.fraudguardian.util.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class CallStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val rawNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            Log.d("CallStateReceiver", "📞 Call state changed: $state, raw=$rawNumber")

            if (!state.equals("RINGING", ignoreCase = true)) return

            val incoming = rawNumber?.replace("[^\\d]".toRegex(), "") ?: ""
            if (incoming.isBlank() || incoming == "Unknown") {
                recordCallEvent(context, incoming, "Unknown or blank")
                return
            }

            recordCallEvent(context, incoming, "Ringing")

            val inContacts = isNumberInContacts(context, incoming)
            if (inContacts) {
                recordCallEvent(context, incoming, "Known contact")
                return
            }

            // =============================
            // HINDI TOGGLE LOGIC
            // =============================
            val prefs = PreferencesManager(context)
            val isHindi = prefs.isHindiVoiceEnabled()
            val msg = if (isHindi) {
                "सावधान! यह नंबर आपके संपर्कों में नहीं है। $incoming"
            } else {
                "Caution! This number is not in your contacts. $incoming"
            }

            val ttsIntent = Intent(context, com.example.fraudguardian.services.TtsAlertService::class.java).apply {
                putExtra("message", msg)
            }
            try { context.startForegroundService(ttsIntent) } catch (e: Exception) { context.startService(ttsIntent) }

            val popup = Intent(context, com.example.fraudguardian.ui.AlertActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("message", msg)
                putExtra("from", incoming)
                putExtra("isCall", true)
            }
            context.startActivity(popup)

            SosScheduler.scheduleSos(context, "Unknown call from $incoming")

        } catch (e: Exception) {
            Log.e("CallStateReceiver", "Error in onReceive", e)
        }
    }

    private fun recordCallEvent(context: Context, number: String, status: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getInstance(context).appDao().insertOtp(
                    OtpLog(
                        sender = number.ifEmpty { "Unknown" },
                        message = "Call event: $status",
                        code = null,
                        receivedAt = Date().time
                    )
                )
            } catch (e: Exception) {
                Log.e("CallStateReceiver", "Error saving call log", e)
            }
        }
    }

    private fun isNumberInContacts(context: Context, phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return false
        val cleaned = phoneNumber.replace("[^\\d]".toRegex(), "").removePrefix("91").takeLast(10)
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val num = it.getString(0)
                    ?.replace("[^\\d]".toRegex(), "")
                    ?.removePrefix("91")
                    ?.takeLast(10)
                    ?: continue
                if (cleaned == num) return true
            }
        }
        return false
    }
}
