package com.example.jetpackcomposetrae20260119

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.jetpackcomposetrae20260119.ui.SubscriptionScreen
import com.example.jetpackcomposetrae20260119.ui.SubscriptionViewModel
import com.example.jetpackcomposetrae20260119.ui.theme.Jetpackcomposetrae20260119Theme
import com.example.jetpackcomposetrae20260119.worker.NotificationWorker
import com.example.jetpackcomposetrae20260119.worker.WorkerScheduler

class MainActivity : ComponentActivity() {
    private val viewModel: SubscriptionViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            checkUpcomingSubscriptions()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkPermissionsAndNotify()
        WorkerScheduler.scheduleDailyCheck(this)

        setContent {
            Jetpackcomposetrae20260119Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SubscriptionScreen(viewModel)
                }
            }
        }
    }

    private fun checkPermissionsAndNotify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                checkUpcomingSubscriptions()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            checkUpcomingSubscriptions()
        }
    }

    private fun checkUpcomingSubscriptions() {
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>().build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }
}