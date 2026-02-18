package com.example.jetpackcomposetrae20260119.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jetpackcomposetrae20260119.data.Subscription
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Premium color palette
private val GradientStart = Color(0xFF667EEA)
private val GradientEnd = Color(0xFF764BA2)
private val AccentOrange = Color(0xFFFF6B35)
private val AccentRed = Color(0xFFE53E3E)
private val SurfaceLight = Color(0xFFF7FAFC)
private val CardBg = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A202C)
private val TextSecondary = Color(0xFF718096)
private val WarningBg = Color(0xFFFFF5F5)
private val WarningBorder = Color(0xFFFEB2B2)
private val SuccessGreen = Color(0xFF38A169)

@OptIn(ExperimentalMaterial3Api::class)
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
            .background(SurfaceLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(
                        text = "📋 訂閱管理",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "共 ${subscriptions.size} 個訂閱項目",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = GradientStart,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "載入中...",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // In-app notification banner for upcoming subscriptions
                    if (upcomingSubscriptions.isNotEmpty()) {
                        item {
                            UpcomingNotificationBanner(upcomingSubscriptions)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    if (subscriptions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "尚無訂閱紀錄",
                                    color = TextSecondary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        items(subscriptions) { subscription ->
                            SubscriptionItem(subscription)
                        }
                    }

                    // Bottom spacing for navigation bar
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingNotificationBanner(upcoming: List<Subscription>) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFF5F5),
                            Color(0xFFFED7D7).copy(alpha = alpha)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AccentRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⚠️ ${upcoming.size} 個訂閱即將到期",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentRed
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            upcoming.forEach { sub ->
                val dateStr = try {
                    ZonedDateTime.parse(sub.nextDate)
                        .format(DateTimeFormatter.ofPattern("MM/dd"))
                } catch (e: Exception) {
                    sub.nextDate.take(5)
                }
                val daysLeft = try {
                    val itemDate = ZonedDateTime.parse(sub.nextDate).toLocalDate()
                    ChronoUnit.DAYS.between(LocalDate.now(), itemDate)
                } catch (e: Exception) {
                    -1L
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (daysLeft <= 1) AccentRed else AccentOrange
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sub.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$dateStr",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (daysLeft <= 1) AccentRed.copy(alpha = 0.15f) else AccentOrange.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (daysLeft == 0L) "今天" else "${daysLeft}天",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (daysLeft <= 1) AccentRed else AccentOrange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionItem(subscription: Subscription) {
    val daysUntilDue = try {
        val itemDate = ZonedDateTime.parse(subscription.nextDate).toLocalDate()
        ChronoUnit.DAYS.between(LocalDate.now(), itemDate)
    } catch (e: Exception) {
        Long.MAX_VALUE
    }

    val isUrgent = daysUntilDue in 0..3
    val borderColor = when {
        daysUntilDue < 0 -> TextSecondary
        daysUntilDue <= 1 -> AccentRed
        daysUntilDue <= 3 -> AccentOrange
        else -> Color.Transparent
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBg
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isUrgent) 8.dp else 3.dp,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left accent bar
            if (borderColor != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(borderColor)
                )
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Header row: name + price
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Colored dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(GradientStart)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = subscription.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GradientStart.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "$${subscription.price}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GradientStart,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Date row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    val formattedDate = try {
                        ZonedDateTime.parse(subscription.nextDate)
                            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                    } catch (e: Exception) {
                        subscription.nextDate
                    }

                    Text(
                        text = "下次付款：$formattedDate",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    if (isUrgent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (daysUntilDue <= 1) AccentRed.copy(alpha = 0.12f) else AccentOrange.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (daysUntilDue == 0L) "⏰ 今天到期" else "⏰ ${daysUntilDue}天後",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (daysUntilDue <= 1) AccentRed else AccentOrange,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Account
                if (subscription.account.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👤",
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = subscription.account,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Site
                if (subscription.site.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🔗 ${subscription.site}",
                        fontSize = 12.sp,
                        color = GradientStart,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Note
                if (subscription.note.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceLight
                    ) {
                        Text(
                            text = "📝 ${subscription.note}",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
