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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.jetpackcomposetrae20260119.ui.OilMonitoringScreen
import com.example.jetpackcomposetrae20260119.ui.OilPriceViewModel
import com.example.jetpackcomposetrae20260119.ui.SubscriptionScreen
import com.example.jetpackcomposetrae20260119.ui.SubscriptionViewModel
import com.example.jetpackcomposetrae20260119.ui.theme.Cloud
import com.example.jetpackcomposetrae20260119.ui.theme.Copper
import com.example.jetpackcomposetrae20260119.ui.theme.Fog
import com.example.jetpackcomposetrae20260119.ui.theme.Ink
import com.example.jetpackcomposetrae20260119.ui.theme.Jetpackcomposetrae20260119Theme
import com.example.jetpackcomposetrae20260119.ui.theme.Midnight
import com.example.jetpackcomposetrae20260119.ui.theme.Mist
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Mist
                ) {
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
            if (
                ContextCompat.checkSelfPermission(
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
    val subscriptions by subscriptionViewModel.subscriptions.collectAsState()
    val upcoming by subscriptionViewModel.upcomingSubscriptions.collectAsState()
    val latest by oilPriceViewModel.latest.collectAsState()

    val tabs = listOf(
        HomeTab("訂閱總覽", "Renewals", Icons.AutoMirrored.Filled.List),
        HomeTab("油價監測", "OQD Tracker", Icons.Default.Refresh)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Mist)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(34.dp),
                color = Midnight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Atlas Monitor",
                                style = MaterialTheme.typography.labelMedium,
                                color = Fog.copy(alpha = 0.72f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "用更清楚的視角追蹤續訂壓力與市場價格",
                                style = MaterialTheme.typography.displayMedium,
                                color = Fog
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "首頁先給摘要，再進入細節畫面，減少閱讀切換成本。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Fog.copy(alpha = 0.74f)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Fog.copy(alpha = 0.08f),
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Copper,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HeroMetric(
                            label = "訂閱數",
                            value = subscriptions.size.toString(),
                            note = if (upcoming.isEmpty()) "近期穩定" else "${upcoming.size} 筆即將到期",
                            modifier = Modifier.weight(1f)
                        )
                        HeroMetric(
                            label = "最新油價",
                            value = latest?.let { "$${"%.2f".format(it.price)}" } ?: "--",
                            note = latest?.displayDate ?: "等待同步",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Fog,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, tab ->
                        DashboardTab(
                            tab = tab,
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "home_content"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> SubscriptionScreen(subscriptionViewModel)
                    else -> OilMonitoringScreen(oilPriceViewModel)
                }
            }
        }
    }
}

private data class HomeTab(
    val title: String,
    val eyebrow: String,
    val icon: ImageVector
)

@Composable
private fun DashboardTab(
    tab: HomeTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Midnight else Fog,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (selected) Copper.copy(alpha = 0.18f) else Cloud.copy(alpha = 0.35f)
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.title,
                    tint = if (selected) Copper else Ink,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = tab.eyebrow,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) Fog.copy(alpha = 0.68f) else Midnight.copy(alpha = 0.62f)
                )
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) Fog else Ink
                )
            }
        }
    }
}

@Composable
private fun HeroMetric(
    label: String,
    value: String,
    note: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Fog.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Fog.copy(alpha = 0.72f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = Fog
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = Fog.copy(alpha = 0.74f)
            )
        }
    }
}
