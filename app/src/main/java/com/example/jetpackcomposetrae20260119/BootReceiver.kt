package com.example.jetpackcomposetrae20260119

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.jetpackcomposetrae20260119.worker.NotificationWorker
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "SubscriptionCheck",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
