package com.example.fraudguardian.workers


import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fraudguardian.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SosWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {



    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {


            val message = inputData.getString("message") ?: "SOS: I might be in trouble. Please check."
            val db = AppDatabase.getInstance(applicationContext).appDao()
            val trusted = db.getTrusted()
            val sms = SmsManager.getSmsManagerForSubscriptionId(SmsManager.getDefaultSmsSubscriptionId())

            trusted.forEach { contact ->
                try {
                    sms.sendTextMessage(contact.number, null, message, null, null)
                } catch (e: Exception) {
                    Log.e("SosWorker", "send failed to ${contact.number}", e)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

