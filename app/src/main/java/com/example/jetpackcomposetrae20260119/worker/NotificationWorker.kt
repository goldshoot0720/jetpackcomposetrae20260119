package com.example.jetpackcomposetrae20260119.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jetpackcomposetrae20260119.R
import com.example.jetpackcomposetrae20260119.data.AppwriteRepository

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val repository = AppwriteRepository(applicationContext)
        val upcoming = repository.getUpcomingSubscriptions(3)

        if (upcoming.isNotEmpty()) {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            upcoming.forEach { sub ->
                val dateStr = try {
                    ZonedDateTime.parse(sub.nextDate).format(formatter)
                } catch (e: Exception) {
                    sub.nextDate.take(10)
                }
                showNotification(
                    notificationId = sub.id.hashCode(),
                    title = "訂閱即將到期：${sub.name}",
                    content = "到期日：$dateStr ｜ 金額：$${sub.price}"
                )
            }
        }

        return Result.success()
    }

    private fun showNotification(notificationId: Int, title: String, content: String) {
        val channelId = "subscription_channel_high"
        val context = applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "訂閱到期提醒"
            val descriptionText = "3天內即將到期的訂閱通知"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}
