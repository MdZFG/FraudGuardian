package com.example.fraudguardian.services

import android.content.Context
import androidx.work.*
import com.example.fraudguardian.workers.SosWorker
import java.util.concurrent.TimeUnit

object SosScheduler {
    private const val SOS_WORK_TAG = "fraudguardian_sos"

    fun scheduleSos(context: Context, message: String, delaySeconds: Long = 5) {
        val data = Data.Builder().putString("message", message).build()
        val request = OneTimeWorkRequestBuilder<SosWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setInputData(data)
            .addTag(SOS_WORK_TAG)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(SOS_WORK_TAG, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancelAllSos(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SOS_WORK_TAG)
    }
}