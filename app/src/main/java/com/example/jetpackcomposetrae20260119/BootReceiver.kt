package com.example.jetpackcomposetrae20260119

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.jetpackcomposetrae20260119.worker.NotificationWorker
import com.example.jetpackcomposetrae20260119.worker.WorkerScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Only schedule background worker, do NOT launch the app UI
            WorkerScheduler.scheduleDailyCheck(context)

            // Also trigger an immediate check in background (no app launch needed)
            val immediateCheck = OneTimeWorkRequestBuilder<NotificationWorker>().build()
            WorkManager.getInstance(context).enqueue(immediateCheck)
        }
    }
}
