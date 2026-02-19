package com.example.jetpackcomposetrae20260119.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object WorkerScheduler {
    fun scheduleDailyCheck(context: Context) {
        val now = LocalDateTime.now()
        var nextRun = now.with(LocalTime.of(6, 0))
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1)
        }

        val initialDelay = ChronoUnit.MILLIS.between(now, nextRun)

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "SubscriptionCheck",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }
}
