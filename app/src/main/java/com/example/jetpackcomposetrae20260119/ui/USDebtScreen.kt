package com.example.jetpackcomposetrae20260119.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jetpackcomposetrae20260119.data.USDebtPoint
import com.example.jetpackcomposetrae20260119.data.USDebtRepository
import com.example.jetpackcomposetrae20260119.ui.theme.Copper
import com.example.jetpackcomposetrae20260119.ui.theme.Fog
import com.example.jetpackcomposetrae20260119.ui.theme.Garnet
import com.example.jetpackcomposetrae20260119.ui.theme.Ink
import com.example.jetpackcomposetrae20260119.ui.theme.Midnight
import com.example.jetpackcomposetrae20260119.ui.theme.Moss
import com.example.jetpackcomposetrae20260119.ui.theme.Porcelain
import com.example.jetpackcomposetrae20260119.ui.theme.Sand
import com.example.jetpackcomposetrae20260119.ui.theme.Slate
import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun USDebtScreen(
    viewModel: USDebtViewModel
) {
    val latest by viewModel.latest.collectAsState()
    val history by viewModel.history.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SourceCard(
                    isLoading = isLoading,
                    onRefresh = viewModel::refreshNationalDebt
                )
            }

            item {
                if (latest != null) {
                    LatestDebtCard(
                        latest = latest!!,
                        sampleCount = history.size,
                        isLoading = isLoading,
                        onRefresh = viewModel::refreshNationalDebt
                    )
                } else {
                    EmptyDebtState(
                        errorMessage = errorMessage,
                        isLoading = isLoading,
                        onRefresh = viewModel::refreshNationalDebt
                    )
                }
            }

            if (history.size >= 2) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(30.dp),
                        color = Porcelain
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "歷史走勢",
                                style = MaterialTheme.typography.titleLarge,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "圖表顯示的是本機累積的抓取點，不是官方歷史資料回補。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            USDebtChart(history = history)
                        }
                    }
                }
            } else {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(30.dp),
                        color = Porcelain
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "歷史走勢",
                                style = MaterialTheme.typography.titleLarge,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "還沒有足夠的 US Debt 歷史樣本，至少要成功抓取兩次才會開始畫圖。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate
                            )
                        }
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFFF6E5E1)
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            modifier = Modifier.padding(16.dp),
                            color = Garnet
                        )
                    }
                }
            }

            if (history.isNotEmpty()) {
                item {
                    val recentPoints = history.takeLast(8).reversed()
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(30.dp),
                        color = Porcelain
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "近期樣本",
                                style = MaterialTheme.typography.titleLarge,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            recentPoints.forEachIndexed { index, point ->
                                RecentDebtRow(point)
                                if (index != recentPoints.lastIndex) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Fog)
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (isLoading && latest == null && history.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Copper
            )
        }
    }
}

@Composable
private fun SourceCard(
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(30.dp),
        color = Porcelain
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
            Text(
                text = "US Debt Clock",
                style = MaterialTheme.typography.labelMedium,
                color = Copper
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "追蹤 US National Debt 的即時估算值",
                style = MaterialTheme.typography.headlineLarge,
                color = Ink
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "資料來源是 usdebtclock.org 首頁頁面，並非官方 API。若網站結構變動，抓取可能暫時失敗。",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate
            )
            Spacer(modifier = Modifier.height(18.dp))

            FilledTonalButton(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = if (isLoading) "抓取中..." else "重新抓取")
            }
        }
    }
}

@Composable
private fun LatestDebtCard(
    latest: USDebtPoint,
    sampleCount: Int,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(30.dp),
        color = Midnight
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "US National Debt",
                        style = MaterialTheme.typography.labelMedium,
                        color = Fog.copy(alpha = 0.72f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatDebtCompact(latest.debt),
                        style = MaterialTheme.typography.displayMedium,
                        color = Fog
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatDebtExact(latest.debt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Fog.copy(alpha = 0.72f)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Fog.copy(alpha = 0.08f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                        imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Copper
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            DebtMetaRow("最後更新", USDebtRepository.formatCapturedAt(latest.capturedAt))
            Spacer(modifier = Modifier.height(8.dp))
            DebtMetaRow("歷史樣本", sampleCount.toString())
            Spacer(modifier = Modifier.height(8.dp))
            DebtMetaRow("資料來源", "usdebtclock.org")

            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = if (isLoading) "抓取中..." else "重新抓取")
            }
        }
    }
}

@Composable
private fun EmptyDebtState(
    errorMessage: String?,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(30.dp),
        color = Porcelain
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Fog,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Copper
                        )
                    }
                }
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(
                        text = "尚未取得資料",
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink
                    )
                    Text(
                        text = "先重新抓取一次 US National Debt",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = errorMessage ?: "目前還沒有可用的 US Debt 資料。",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate
            )

            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(
                onClick = onRefresh,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = if (isLoading) "抓取中..." else "重新抓取")
            }
        }
    }
}

@Composable
private fun DebtMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Fog.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            color = Fog,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun USDebtChart(history: List<USDebtPoint>) {
    val chartPoints = history.takeLast(40)
    val values = chartPoints.map { it.debt }
    val minDebt = values.minOrNull() ?: 0.0
    val maxDebt = values.maxOrNull() ?: 0.0
    val range = (maxDebt - minDebt).takeIf { it > 0.0 } ?: 1.0

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val leftPadding = 24.dp.toPx()
            val topPadding = 16.dp.toPx()
            val rightPadding = 12.dp.toPx()
            val bottomPadding = 24.dp.toPx()
            val chartWidth = size.width - leftPadding - rightPadding
            val chartHeight = size.height - topPadding - bottomPadding

            drawRoundRect(
                color = Sand.copy(alpha = 0.55f),
                topLeft = Offset(leftPadding, topPadding),
                size = Size(chartWidth, chartHeight),
                cornerRadius = CornerRadius(24f, 24f)
            )

            repeat(4) { index ->
                val y = topPadding + chartHeight * (index / 3f)
                drawLine(
                    color = Fog,
                    start = Offset(leftPadding, y),
                    end = Offset(leftPadding + chartWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val offsets = chartPoints.mapIndexed { index, point ->
                val x = if (chartPoints.size == 1) {
                    leftPadding + chartWidth / 2f
                } else {
                    leftPadding + chartWidth * index / chartPoints.lastIndex.toFloat()
                }
                val normalized = ((point.debt - minDebt) / range).toFloat()
                val y = topPadding + chartHeight - (chartHeight * normalized)
                Offset(x, y)
            }

            val fillPath = Path().apply {
                val first = offsets.first()
                moveTo(first.x, topPadding + chartHeight)
                offsets.forEach { offset -> lineTo(offset.x, offset.y) }
                val last = offsets.last()
                lineTo(last.x, topPadding + chartHeight)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Copper.copy(alpha = 0.32f), Color.Transparent)
                )
            )

            val linePath = Path().apply {
                offsets.forEachIndexed { index, offset ->
                    if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                }
            }

            drawPath(
                path = linePath,
                color = Copper,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            offsets.forEach { offset ->
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = offset
                )
                drawCircle(
                    color = Moss,
                    radius = 3.dp.toPx(),
                    center = offset
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = chartPoints.first().capturedAt.toShortCapturedAt(),
                color = Slate,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Low ${formatDebtCompact(minDebt)}",
                color = Slate,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "High ${formatDebtCompact(maxDebt)}",
                color = Slate,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = chartPoints.last().capturedAt.toShortCapturedAt(),
                color = Slate,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RecentDebtRow(point: USDebtPoint) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = point.capturedAt.toDisplayCapturedAt(),
                color = Ink,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatDebtExact(point.debt),
                color = Slate,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Sand
        ) {
            Text(
                text = formatDebtCompact(point.debt),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                color = Midnight
            )
        }
    }
}

private fun formatDebtCompact(value: Double): String {
    val trillions = value / 1_000_000_000_000.0
    return String.format(Locale.US, "$%.2fT", trillions)
}

private fun formatDebtExact(value: Double): String {
    return "$" + DecimalFormat("#,###").format(value)
}

private fun USDebtPoint.toShortCapturedAt(): String {
    return capturedAt.toInstantOrNull()
        ?.atZone(ZoneId.systemDefault())
        ?.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
        ?: "--"
}

private fun String.toDisplayCapturedAt(): String {
    return toInstantOrNull()
        ?.atZone(ZoneId.systemDefault())
        ?.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
        ?: this
}

private fun String.toShortCapturedAt(): String {
    return toInstantOrNull()
        ?.atZone(ZoneId.systemDefault())
        ?.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
        ?: this
}

private fun String.toInstantOrNull(): Instant? = runCatching { Instant.parse(this) }.getOrNull()
