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
        val initialDelay = computeInitialDelay(LocalTime.of(5, 19))
        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "SubscriptionCheck",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun scheduleDailyOilFetch(context: Context) {
        val initialDelay = computeInitialDelay(LocalTime.of(13, 0))
        val workRequest = PeriodicWorkRequestBuilder<OilPriceWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "OilPriceFetch",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun computeInitialDelay(runAt: LocalTime): Long {
        val now = LocalDateTime.now()
        var nextRun = now.with(runAt)
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1)
        }

        return ChronoUnit.MILLIS.between(now, nextRun)
    }
}
