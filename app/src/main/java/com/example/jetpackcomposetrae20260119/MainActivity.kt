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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.jetpackcomposetrae20260119.ui.OilMonitoringScreen
import com.example.jetpackcomposetrae20260119.ui.OilPriceViewModel
import com.example.jetpackcomposetrae20260119.ui.SubscriptionScreen
import com.example.jetpackcomposetrae20260119.ui.SubscriptionViewModel
import com.example.jetpackcomposetrae20260119.ui.theme.Jetpackcomposetrae20260119Theme
import com.example.jetpackcomposetrae20260119.worker.NotificationWorker
import com.example.jetpackcomposetrae20260119.worker.WorkerScheduler

class MainActivity : ComponentActivity() {
    private val viewModel: SubscriptionViewModel by viewModels()
    private val oilPriceViewModel: OilPriceViewModel by viewModels()

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
        WorkerScheduler.scheduleDailyOilFetch(this)

        setContent {
            Jetpackcomposetrae20260119Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        subscriptionViewModel = viewModel,
                        oilPriceViewModel = oilPriceViewModel
                    )
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

@Composable
private fun HomeScreen(
    subscriptionViewModel: SubscriptionViewModel,
    oilPriceViewModel: OilPriceViewModel
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("订阅", "石油监控")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    icon = {
                        Icon(
                            imageVector = if (index == 0) Icons.Default.List else Icons.Default.Refresh,
                            contentDescription = title
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> SubscriptionScreen(subscriptionViewModel)
            else -> OilMonitoringScreen(oilPriceViewModel)
        }
    }
}
