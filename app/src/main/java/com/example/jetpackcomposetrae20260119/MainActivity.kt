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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.jetpackcomposetrae20260119.ui.LotteryComparisonScreen
import com.example.jetpackcomposetrae20260119.ui.LotteryComparisonViewModel
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

class MainActivity : ComponentActivity() {
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private val oilPriceViewModel: OilPriceViewModel by viewModels()
    private val usDebtViewModel: USDebtViewModel by viewModels()
    private val lotteryComparisonViewModel: LotteryComparisonViewModel by viewModels()

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
                        usDebtViewModel = usDebtViewModel,
                        lotteryComparisonViewModel = lotteryComparisonViewModel
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
    oilPriceViewModel: OilPriceViewModel,
    usDebtViewModel: USDebtViewModel,
    lotteryComparisonViewModel: LotteryComparisonViewModel
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        HomeTab("訂閱提醒", "Renewals", Icons.AutoMirrored.Filled.List),
        HomeTab("油價觀測", "Oil", Icons.Default.Refresh),
        HomeTab("美債追蹤", "US Debt", Icons.Default.DateRange),
        HomeTab("最瞎結婚理由", "Lottery", Icons.Default.Info)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Mist)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Atlas Monitor",
                    style = MaterialTheme.typography.labelMedium,
                    color = Midnight.copy(alpha = 0.68f)
                )
                Text(
                    text = "生活資料儀表板",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink
                )
            }

            Surface(
                shape = CircleShape,
                color = Fog,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Copper
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(top = 14.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
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
                    MenuTab(
                        tab = tab,
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(top = 14.dp))

        Text(
            text = "選擇想查看的資料卡",
            style = MaterialTheme.typography.labelLarge,
            color = Midnight.copy(alpha = 0.62f)
        )

        Spacer(modifier = Modifier.padding(top = 10.dp))

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "home_content",
            modifier = Modifier.weight(1f)
        ) { tabIndex ->
            when (tabIndex) {
                0 -> SubscriptionScreen(subscriptionViewModel)
                1 -> OilMonitoringScreen(oilPriceViewModel)
                2 -> USDebtScreen(usDebtViewModel)
                else -> LotteryComparisonScreen(lotteryComparisonViewModel)
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
private fun MenuTab(
    tab: HomeTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) Midnight else Fog,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (selected) Copper.copy(alpha = 0.18f) else Cloud.copy(alpha = 0.35f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (selected) Copper else Ink
                    )
                }
            }

            Text(
                text = tab.eyebrow,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Fog.copy(alpha = 0.68f) else Midnight.copy(alpha = 0.62f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = tab.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) Fog else Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
