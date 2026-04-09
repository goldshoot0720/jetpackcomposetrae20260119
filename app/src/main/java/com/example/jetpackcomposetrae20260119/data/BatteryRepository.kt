package com.example.jetpackcomposetrae20260119.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class BatteryRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBatterySnapshot(): BatterySnapshot? {
        val intent = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return null

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return null

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val currentLevelPercent = ((level * 100f) / scale).toInt().coerceIn(0, 100)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val isFullNow = currentLevelPercent >= 100 || status == BatteryManager.BATTERY_STATUS_FULL

        val previousWasFull = prefs.getBoolean(KEY_PREVIOUS_WAS_FULL, false)
        var lastFullChargeAt = prefs.getLong(KEY_LAST_FULL_CHARGE_AT, 0L).takeIf { it > 0L }

        if (isFullNow && !previousWasFull) {
            lastFullChargeAt = System.currentTimeMillis()
            prefs.edit()
                .putLong(KEY_LAST_FULL_CHARGE_AT, lastFullChargeAt)
                .putBoolean(KEY_PREVIOUS_WAS_FULL, true)
                .apply()
        } else if (!isFullNow && previousWasFull) {
            prefs.edit().putBoolean(KEY_PREVIOUS_WAS_FULL, false).apply()
        }

        val estimatedMillisToFull = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isCharging) {
            val batteryManager = appContext.getSystemService(BatteryManager::class.java)
            batteryManager?.computeChargeTimeRemaining()?.takeIf { it > 0L }
        } else {
            null
        }

        val estimatedTargetTimeMillis = estimatedMillisToFull?.let { System.currentTimeMillis() + it }
        val estimatedTargetLabel = when {
            estimatedTargetTimeMillis != null -> "預估充滿時間"
            isCharging -> "預估充滿時間"
            else -> "預估電量時間"
        }

        return BatterySnapshot(
            lastFullChargeAt = lastFullChargeAt,
            currentLevelPercent = currentLevelPercent,
            estimatedTargetTimeMillis = estimatedTargetTimeMillis,
            estimatedTargetLabel = estimatedTargetLabel,
            isCharging = isCharging
        )
    }

    companion object {
        private const val PREFS_NAME = "battery_panel"
        private const val KEY_LAST_FULL_CHARGE_AT = "last_full_charge_at"
        private const val KEY_PREVIOUS_WAS_FULL = "previous_was_full"

        private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

        fun formatTimestamp(timestampMillis: Long?): String {
            if (timestampMillis == null) return "--"
            return timestampFormatter.format(
                Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault())
            )
        }

        fun formatRelativeDifference(targetMillis: Long?): String {
            if (targetMillis == null) return "--"

            val deltaMillis = targetMillis - System.currentTimeMillis()
            val absMinutes = TimeUnit.MILLISECONDS.toMinutes(kotlin.math.abs(deltaMillis))
            val hours = absMinutes / 60
            val minutes = absMinutes % 60
            val durationText = buildString {
                if (hours > 0) append("${hours} 小時 ")
                append("${minutes} 分鐘")
            }

            return if (deltaMillis >= 0) {
                "$durationText 後"
            } else {
                "$durationText 前"
            }
        }
    }
}
