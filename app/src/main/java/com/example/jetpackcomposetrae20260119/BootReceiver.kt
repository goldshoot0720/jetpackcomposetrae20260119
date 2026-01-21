package com.example.jetpackcomposetrae20260119

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.jetpackcomposetrae20260119.worker.WorkerScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            WorkerScheduler.scheduleDailyCheck(context)
            
            // Launch the app
            val launchIntent = Intent(context, MainActivity::class.java)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }
    }
}
