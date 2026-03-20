package com.example.jetpackcomposetrae20260119.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.jetpackcomposetrae20260119.data.Subscription
import com.example.jetpackcomposetrae20260119.ui.theme.Copper
import com.example.jetpackcomposetrae20260119.ui.theme.Fog
import com.example.jetpackcomposetrae20260119.ui.theme.Garnet
import com.example.jetpackcomposetrae20260119.ui.theme.Ink
import com.example.jetpackcomposetrae20260119.ui.theme.Midnight
import com.example.jetpackcomposetrae20260119.ui.theme.Moss
import com.example.jetpackcomposetrae20260119.ui.theme.Porcelain
import com.example.jetpackcomposetrae20260119.ui.theme.Sand
import com.example.jetpackcomposetrae20260119.ui.theme.Slate
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    val upcomingSubscriptions by viewModel.upcomingSubscriptions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SubscriptionOverview(
                    totalCount = subscriptions.size,
                    urgentCount = upcomingSubscriptions.size
                )
            }

            if (upcomingSubscriptions.isNotEmpty()) {
                item {
                    UpcomingNotificationBanner(upcomingSubscriptions)
                }
            }

            if (isLoading && subscriptions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Copper, strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "正在整理你的訂閱資料",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate
                            )
                        }
                    }
                }
            } else if (subscriptions.isEmpty()) {
                item {
                    EmptySubscriptionState()
                }
            } else {
                items(subscriptions) { subscription ->
                    SubscriptionItem(subscription)
                }
            }
        }
    }
}

@Composable
private fun SubscriptionOverview(
    totalCount: Int,
    urgentCount: Int
) {
    Surface(
        shape = RoundedCornerShape(30.dp),
        color = Porcelain,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Text(
                text = "Subscription Ledger",
                style = MaterialTheme.typography.labelMedium,
                color = Copper
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "把即將續訂的壓力，改成可以提早處理的節奏",
                style = MaterialTheme.typography.headlineLarge,
                color = Ink
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "畫面分成總覽、提醒和單筆資訊，減少混雜的視線跳轉。",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OverviewBlock(
                    label = "總筆數",
                    value = totalCount.toString(),
                    tone = Midnight,
                    modifier = Modifier.weight(1f)
                )
                OverviewBlock(
                    label = "三日內",
                    value = urgentCount.toString(),
                    tone = if (urgentCount > 0) Garnet else Moss,
                    modifier = Modifier.weight(1f)
                )
                OverviewBlock(
                    label = "狀態",
                    value = if (urgentCount > 0) "提醒中" else "穩定",
                    tone = Copper,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OverviewBlock(
    label: String,
    value: String,
    tone: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Fog,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Slate
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = tone
            )
        }
    }
}

@Composable
fun UpcomingNotificationBanner(upcoming: List<Subscription>) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFFF5E9E6),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Garnet
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "接下來 3 天內有 ${upcoming.size} 筆續訂需要注意",
                    style = MaterialTheme.typography.titleMedium,
                    color = Garnet
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            upcoming.forEach { sub ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sub.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Ink
                        )
                        Text(
                            text = formatSubscriptionDate(sub.nextDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate
                        )
                    }
                    DueBadge(daysUntilDue = daysUntil(sub.nextDate))
                }
            }
        }
    }
}

@Composable
private fun EmptySubscriptionState() {
    Surface(
        shape = RoundedCornerShape(30.dp),
        color = Porcelain,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 30.dp)
        ) {
            Text(
                text = "目前沒有可顯示的訂閱資料",
                style = MaterialTheme.typography.titleLarge,
                color = Ink
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "同步完成後，這裡會用新的版面顯示每一筆續訂的時間壓力。",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate
            )
        }
    }
}

@Composable
fun SubscriptionItem(subscription: Subscription) {
    val daysUntilDue = daysUntil(subscription.nextDate)
    val accent = when {
        daysUntilDue < 0 -> Slate
        daysUntilDue <= 1 -> Garnet
        daysUntilDue <= 3 -> Copper
        else -> Moss
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Porcelain,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "下次扣款 ${formatSubscriptionDate(subscription.nextDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Sand
                ) {
                    Text(
                        text = "$${subscription.price}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetaPill(
                    icon = Icons.Default.DateRange,
                    text = when {
                        daysUntilDue < 0 -> "已過期"
                        daysUntilDue == 0L -> "今天到期"
                        daysUntilDue == Long.MAX_VALUE -> "日期未解析"
                        else -> "$daysUntilDue 天後到期"
                    },
                    accent = accent,
                    modifier = Modifier.weight(1f)
                )

                if (subscription.account.isNotBlank()) {
                    MetaPill(
                        icon = Icons.AutoMirrored.Filled.List,
                        text = subscription.account,
                        accent = Midnight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (subscription.site.isNotBlank() || subscription.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Fog)
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (subscription.site.isNotBlank()) {
                MetaLine(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "網站",
                    value = subscription.site
                )
            }

            if (subscription.note.isNotBlank()) {
                if (subscription.site.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                MetaLine(
                    icon = Icons.Default.Notifications,
                    label = "備註",
                    value = subscription.note
                )
            }
        }
    }
}

@Composable
private fun DueBadge(daysUntilDue: Long) {
    val background = when {
        daysUntilDue <= 1 -> Garnet.copy(alpha = 0.12f)
        daysUntilDue <= 3 -> Copper.copy(alpha = 0.12f)
        else -> Moss.copy(alpha = 0.12f)
    }
    val color = when {
        daysUntilDue <= 1 -> Garnet
        daysUntilDue <= 3 -> Copper
        else -> Moss
    }
    val content = when {
        daysUntilDue < 0 -> "已過期"
        daysUntilDue == 0L -> "今天"
        daysUntilDue == Long.MAX_VALUE -> "未定"
        else -> "$daysUntilDue 天"
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = background
    ) {
        Text(
            text = content,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

@Composable
private fun MetaPill(
    icon: ImageVector,
    text: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Fog,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MetaLine(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Copper,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Slate
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Ink
            )
        }
    }
}

private fun formatSubscriptionDate(raw: String): String {
    return try {
        ZonedDateTime.parse(raw).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    } catch (_: Exception) {
        raw
    }
}

private fun daysUntil(raw: String): Long {
    return try {
        val itemDate = ZonedDateTime.parse(raw).toLocalDate()
        ChronoUnit.DAYS.between(LocalDate.now(), itemDate)
    } catch (_: Exception) {
        Long.MAX_VALUE
    }
}
