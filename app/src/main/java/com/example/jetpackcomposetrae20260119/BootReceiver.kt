package com.example.jetpackcomposetrae20260119

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.jetpackcomposetrae20260119.worker.WorkerScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            WorkerScheduler.scheduleDailyCheck(context)
        }
    }
}
