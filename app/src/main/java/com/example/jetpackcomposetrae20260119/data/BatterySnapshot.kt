package com.example.jetpackcomposetrae20260119.data

data class BatterySnapshot(
    val lastFullChargeAt: Long?,
    val currentLevelPercent: Int,
    val estimatedTargetTimeMillis: Long?,
    val estimatedTargetLabel: String,
    val isCharging: Boolean
)
