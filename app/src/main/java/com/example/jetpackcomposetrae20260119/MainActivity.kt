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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.jetpackcomposetrae20260119.ui.OilMonitoringScreen
import com.example.jetpackcomposetrae20260119.ui.OilPriceViewModel
import com.example.jetpackcomposetrae20260119.ui.SubscriptionScreen
import com.example.jetpackcomposetrae20260119.ui.SubscriptionViewModel
import com.example.jetpackcomposetrae20260119.ui.USDebtScreen
import com.example.jetpackcomposetrae20260119.ui.USDebtViewModel
import com.example.jetpackcomposetrae20260119.ui.theme.Cloud
import com.example.jetpackcomposetrae20260119.ui.theme.Copper
import com.example.jetpackcomposetrae20260119.ui.theme.Fog
import com.example.jetpackcomposetrae20260119.ui.theme.Ink
import com.example.jetpackcomposetrae20260119.ui.theme.Jetpackcomposetrae20260119Theme
import com.example.jetpackcomposetrae20260119.ui.theme.Midnight
import com.example.jetpackcomposetrae20260119.ui.theme.Mist
import com.example.jetpackcomposetrae20260119.worker.NotificationWorker
import com.example.jetpackcomposetrae20260119.worker.WorkerScheduler
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private val oilPriceViewModel: OilPriceViewModel by viewModels()
    private val usDebtViewModel: USDebtViewModel by viewModels()

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
                        subscriptionViewModel = subscriptionViewModel,
                        oilPriceViewModel = oilPriceViewModel,
                        usDebtViewModel = usDebtViewModel
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeScreen(
    subscriptionViewModel: SubscriptionViewModel,
    oilPriceViewModel: OilPriceViewModel,
    usDebtViewModel: USDebtViewModel
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val subscriptions by subscriptionViewModel.subscriptions.collectAsState()
    val upcoming by subscriptionViewModel.upcomingSubscriptions.collectAsState()
    val latestOil by oilPriceViewModel.latest.collectAsState()
    val latestDebt by usDebtViewModel.latest.collectAsState()

    val tabs = listOf(
        HomeTab("訂閱總覽", "Renewals", Icons.AutoMirrored.Filled.List),
        HomeTab("油價監測", "Oil Tracker", Icons.Default.Refresh),
        HomeTab("美債時鐘", "US Debt", Icons.Default.DateRange)
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Mist)
    ) {
        val isCompact = maxWidth < 430.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = if (isCompact) 14.dp else 18.dp, vertical = 14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(if (isCompact) 30.dp else 34.dp),
                color = Midnight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = if (isCompact) 18.dp else 22.dp,
                        vertical = if (isCompact) 20.dp else 22.dp
                    )
                ) {
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
                                text = "掌握訂閱、油價與美國債務的即時脈動",
                                style = if (isCompact) {
                                    MaterialTheme.typography.displaySmall
                                } else {
                                    MaterialTheme.typography.displayMedium
                                },
                                color = Fog
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "在同一個首頁快速查看續訂提醒、最新油價與 US National Debt，重要變化一眼就能追上。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Fog.copy(alpha = 0.74f)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Fog.copy(alpha = 0.08f),
                            modifier = Modifier.padding(start = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Copper,
                                modifier = Modifier.padding(if (isCompact) 12.dp else 14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        maxItemsInEachRow = if (isCompact) 2 else 3
                    ) {
                        HeroMetric(
                            label = "訂閱數量",
                            value = subscriptions.size.toString(),
                            note = if (upcoming.isEmpty()) {
                                "目前沒有即將到期項目"
                            } else {
                                "${upcoming.size} 個項目即將續訂"
                            },
                            modifier = if (isCompact) Modifier.fillMaxWidth(0.48f) else Modifier.fillMaxWidth(0.31f)
                        )
                        HeroMetric(
                            label = "最新油價",
                            value = latestOil?.let { "$${"%.2f".format(it.price)}" } ?: "--",
                            note = latestOil?.displayDate ?: "尚未同步資料",
                            modifier = if (isCompact) Modifier.fillMaxWidth(0.48f) else Modifier.fillMaxWidth(0.31f)
                        )
                        HeroMetric(
                            label = "US Debt",
                            value = latestDebt?.let { formatDebtCompact(it.debt) } ?: "--",
                            note = latestDebt?.capturedAt?.toHomeCapturedAt() ?: "尚未同步資料",
                            modifier = if (isCompact) Modifier.fillMaxWidth() else Modifier.fillMaxWidth(0.31f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    DashboardTab(
                        tab = tab,
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "home_content",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> SubscriptionScreen(subscriptionViewModel)
                    1 -> OilMonitoringScreen(oilPriceViewModel)
                    else -> USDebtScreen(usDebtViewModel)
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
        shape = RoundedCornerShape(28.dp),
        color = if (selected) Midnight else Fog,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (selected) Copper.copy(alpha = 0.18f) else Cloud.copy(alpha = 0.35f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (selected) Copper else Ink
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tab.eyebrow,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) Fog.copy(alpha = 0.68f) else Midnight.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (selected) Fog else Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                color = Fog.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = Fog,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
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

private fun formatDebtCompact(value: Double): String {
    return String.format(Locale.US, "$%.2fT", value / 1_000_000_000_000.0)
}

private fun String.toHomeCapturedAt(): String {
    return runCatching {
        Instant.parse(this)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
    }.getOrDefault("尚未同步")
}
