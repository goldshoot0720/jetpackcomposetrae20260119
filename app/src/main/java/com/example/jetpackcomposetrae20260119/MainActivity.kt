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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.jetpackcomposetrae20260119.ui.BatteryScreen
import com.example.jetpackcomposetrae20260119.ui.BatteryViewModel
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val batteryViewModel: BatteryViewModel by viewModels()
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
                        batteryViewModel = batteryViewModel,
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
@OptIn(ExperimentalLayoutApi::class)
private fun HomeScreen(
    batteryViewModel: BatteryViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    oilPriceViewModel: OilPriceViewModel,
    usDebtViewModel: USDebtViewModel,
    lotteryComparisonViewModel: LotteryComparisonViewModel
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val now by produceState(initialValue = LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(60_000)
        }
    }
    val today = now.toLocalDate()
    val birthdayEasterEgg = rememberBirthdayEasterEgg(today)
    val sleepReminder = calculateSleepReminder(now)

    val tabs = listOf(
        HomeTab("電池選單", "Battery", Icons.Default.DateRange),
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
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "home_content",
            modifier = Modifier.fillMaxSize()
        ) { tabIndex ->
            val headerContent: LazyListScope.() -> Unit = {
                homeHeaderSection(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    birthdayEasterEgg = birthdayEasterEgg,
                    sleepReminder = sleepReminder
                )
            }

            when (tabIndex) {
                0 -> BatteryScreen(
                    viewModel = batteryViewModel,
                    headerContent = headerContent
                )
                1 -> SubscriptionScreen(
                    viewModel = subscriptionViewModel,
                    headerContent = headerContent
                )
                2 -> OilMonitoringScreen(
                    viewModel = oilPriceViewModel,
                    headerContent = headerContent
                )
                3 -> USDebtScreen(
                    viewModel = usDebtViewModel,
                    headerContent = headerContent
                )
                else -> LotteryComparisonScreen(
                    viewModel = lotteryComparisonViewModel,
                    headerContent = headerContent
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
private fun LazyListScope.homeHeaderSection(
    tabs: List<HomeTab>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    birthdayEasterEgg: BirthdayEasterEgg?,
    sleepReminder: SleepReminder?
) {
    item {
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
    }

    item {
        Spacer(modifier = Modifier.padding(top = 14.dp))
    }

    item {
        FengBroAsciiCard()
    }

    if (birthdayEasterEgg != null) {
        item {
            Spacer(modifier = Modifier.padding(top = 14.dp))
        }
        item {
            BirthdayEasterEggCard(birthdayEasterEgg)
        }
    }

    if (sleepReminder != null) {
        item {
            Spacer(modifier = Modifier.padding(top = 14.dp))
        }
        item {
            SleepReminderCard(sleepReminder)
        }
    }

    item {
        Spacer(modifier = Modifier.padding(top = 14.dp))
    }

    item {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Fog,
            modifier = Modifier.fillMaxWidth()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                tabs.forEachIndexed { index, tab ->
                    MenuTab(
                        tab = tab,
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        modifier = Modifier.fillMaxWidth(0.31f)
                    )
                }
            }
        }
    }

    item {
        Spacer(modifier = Modifier.padding(top = 14.dp))
    }

    item {
        Text(
            text = "選擇想查看的資料卡",
            style = MaterialTheme.typography.labelLarge,
            color = Midnight.copy(alpha = 0.62f)
        )
    }

    item {
        Spacer(modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun FengBroAsciiCard() {
    val asciiArt = """
        ______                         __               
       / ____/__  ____  ____ _        / /_  _________  
      / /_  / _ \/ __ \/ __ `/ ______/ __ \/ ___/ __ \ 
     / __/ /  __/ / / / /_/ / /_____/ /_/ / /  / /_/ /
    /_/    \___/_/ /_/\__, /       /_.___/_/   \____/ 
                     /____/                           
    """.trimIndent()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Midnight
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "ASCII ART",
                style = MaterialTheme.typography.labelLarge,
                color = CopperGlow
            )
            Text(
                text = asciiArt,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                color = Fog
            )
            Text(
                text = "feng bro",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFFFE08A)
            )
        }
    }
}

@Composable
private fun BirthdayEasterEggCard(easterEgg: BirthdayEasterEgg) {
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
                    text = easterEgg.badge,
                    style = MaterialTheme.typography.labelLarge,
                    color = Midnight.copy(alpha = 0.78f)
                )
                Text(
                    text = easterEgg.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Midnight
                )
                Text(
                    text = easterEgg.subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink
                )
                Text(
                    text = easterEgg.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Midnight.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private data class BirthdayEasterEgg(
    val badge: String,
    val title: String,
    val subtitle: String,
    val description: String
)

private data class SleepReminder(
    val currentDateLabel: String,
    val currentTimeLabel: String,
    val reminderCount: Int,
    val guidance: String
)

private fun rememberBirthdayEasterEgg(today: LocalDate): BirthdayEasterEgg? {
    return when {
        today.monthValue == 4 && today.dayOfMonth == 3 -> BirthdayEasterEgg(
            badge = "4/3 限定彩蛋",
            title = "塗哥生日快樂",
            subtitle = "今彩539頭獎得主鋒兄",
            description = "今天首頁自動開啟生日特效，祝福與好手氣一起加倍。"
        )
        today.monthValue == 11 && today.dayOfMonth == 27 -> BirthdayEasterEgg(
            badge = "11/27 限定彩蛋",
            title = "鋒兄生日快樂",
            subtitle = "高考三級資訊處理榜首鋒兄",
            description = "今天首頁自動開啟鋒兄專屬生日特效，祝福榜首鋒芒繼續一路發光。"
        )
        else -> null
    }
}

@Composable
private fun SleepReminderCard(reminder: SleepReminder) {
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
                            Midnight,
                            Color(0xFF243248),
                            Color(0xFF506A84)
                        ),
                        start = Offset.Zero,
                        end = Offset(900f, 320f)
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "睡眠提示",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFFD27A)
                )
                Text(
                    text = "該準備睡覺了",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Fog
                )
                Text(
                    text = reminder.guidance,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Fog.copy(alpha = 0.78f)
                )
                Spacer(modifier = Modifier.padding(top = 4.dp))
                Text(
                    text = "今天日期 ${reminder.currentDateLabel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "現在時刻 ${reminder.currentTimeLabel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "提示次數 第 ${reminder.reminderCount} 次",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFFFE08A)
                )
            }
        }
    }
}

private fun calculateSleepReminder(now: LocalDateTime): SleepReminder? {
    val hour = now.hour
    val minute = now.minute
    val totalMinutes = hour * 60 + minute
    if (totalMinutes < 0 || totalMinutes > 240) {
        return null
    }

    val reminderCount = when {
        totalMinutes < 120 -> (totalMinutes / 30) + 1
        totalMinutes < 240 -> 5 + ((totalMinutes - 120) / 15)
        else -> 13
    }

    val guidance = when {
        totalMinutes == 0 -> "0 點第一次提示，今天先早點休息。"
        totalMinutes < 120 -> "0 點到 2 點每 30 分鐘提醒一次，現在已經有點晚了。"
        totalMinutes < 240 -> "2 點到 4 點每 15 分鐘提醒一次，真的該去睡了。"
        else -> "4 點最後一次提示，現在該立刻收工休息。"
    }

    return SleepReminder(
        currentDateLabel = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
        currentTimeLabel = now.format(DateTimeFormatter.ofPattern("HH:mm")),
        reminderCount = reminderCount,
        guidance = guidance
    )
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
