package com.example.jetpackcomposetrae20260119.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jetpackcomposetrae20260119.data.OilPriceRepository

class OilPriceWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val point = OilPriceRepository(applicationContext).refreshLatestPrice()
        return if (point != null) Result.success() else Result.retry()
    }
}
