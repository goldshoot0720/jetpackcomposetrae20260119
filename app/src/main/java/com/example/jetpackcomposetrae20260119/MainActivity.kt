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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.jetpackcomposetrae20260119.ui.theme.CopperGlow
import com.example.jetpackcomposetrae20260119.ui.theme.Fog
import com.example.jetpackcomposetrae20260119.ui.theme.Ink
import com.example.jetpackcomposetrae20260119.ui.theme.Jetpackcomposetrae20260119Theme
import com.example.jetpackcomposetrae20260119.ui.theme.Midnight
import com.example.jetpackcomposetrae20260119.ui.theme.Mist
import com.example.jetpackcomposetrae20260119.worker.NotificationWorker
import com.example.jetpackcomposetrae20260119.worker.WorkerScheduler
import java.time.LocalDate

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
    val today = LocalDate.now()
    val showBirthdayEasterEgg = today.monthValue == 4 && today.dayOfMonth == 3

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

        if (showBirthdayEasterEgg) {
            BirthdayEasterEggCard()
            Spacer(modifier = Modifier.padding(top = 14.dp))
        }

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

@Composable
private fun BirthdayEasterEggCard() {
    val transition = rememberInfiniteTransition(label = "birthday_card")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    val shimmerShift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_shift"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            CopperGlow.copy(alpha = 0.95f),
                            Color(0xFFF8D76A),
                            Color(0xFFFFF3C2)
                        ),
                        start = Offset.Zero,
                        end = Offset(900f, 320f)
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = glowAlpha * 0.22f),
                    radius = size.minDimension * 0.24f,
                    center = Offset(size.width * (0.18f + shimmerShift * 0.06f), size.height * 0.26f)
                )
                drawCircle(
                    color = Copper.copy(alpha = glowAlpha * 0.18f),
                    radius = size.minDimension * 0.32f,
                    center = Offset(size.width * 0.88f, size.height * (0.18f + shimmerShift * 0.10f))
                )
                drawCircle(
                    color = Color.White.copy(alpha = glowAlpha * 0.16f),
                    radius = size.minDimension * 0.18f,
                    center = Offset(size.width * 0.72f, size.height * 0.78f)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "4/3 限定彩蛋",
                    style = MaterialTheme.typography.labelLarge,
                    color = Midnight.copy(alpha = 0.78f)
                )
                Text(
                    text = "塗哥生日快樂",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Midnight
                )
                Text(
                    text = "今彩539頭獎得主鋒兄",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink
                )
                Text(
                    text = "今天首頁自動開啟生日特效，祝福與好手氣一起加倍。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Midnight.copy(alpha = 0.8f)
                )
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
