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
import com.example.jetpackcomposetrae20260119.data.OilPricePoint
import com.example.jetpackcomposetrae20260119.data.OilPriceRepository
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
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun OilMonitoringScreen(
    viewModel: OilPriceViewModel
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
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(30.dp),
                    color = Porcelain
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
                        Text(
                            text = "Market Signal",
                            style = MaterialTheme.typography.labelMedium,
                            color = Copper
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "用更平靜的版面讀取 OQD Daily Marker Price 的變化",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "資料來源為 Gulf Mercantile Exchange，支援開啟時與手動同步。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate
                        )
                    }
                }
            }

            item {
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
                            Column {
                                Text(
                                    text = "OQD Daily Marker Price",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Fog.copy(alpha = 0.72f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = latest?.let { "$${String.format(Locale.US, "%.2f", it.price)}" } ?: "--",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = Fog
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = Fog.copy(alpha = 0.08f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Copper
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        PriceMetaRow("交易日期", latest?.displayDate ?: "--")
                        Spacer(modifier = Modifier.height(8.dp))
                        PriceMetaRow(
                            "同步時間",
                            latest?.let { OilPriceRepository.formatFetchedAt(it.fetchedAt) } ?: "--"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PriceMetaRow("歷史筆數", history.size.toString())

                        Spacer(modifier = Modifier.height(16.dp))

                        FilledTonalButton(
                            onClick = viewModel::refreshLatestPrice,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(text = if (isLoading) "同步中..." else "立即更新價格")
                        }
                    }
                }
            }

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
                            text = "近 30 筆趨勢",
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "圖表刻意保持簡潔，優先凸顯方向與幅度。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        OilPriceChart(history = history)
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
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(30.dp),
                        color = Porcelain
                    ) {
                        val recentPoints = history.takeLast(8).reversed()
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "最近記錄",
                                style = MaterialTheme.typography.titleLarge,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            recentPoints.forEachIndexed { index, point ->
                                RecentPriceRow(point)
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

        if (isLoading && history.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Copper
            )
        }
    }
}

@Composable
private fun PriceMetaRow(label: String, value: String) {
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
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun OilPriceChart(history: List<OilPricePoint>) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "還沒有足夠的歷史資料可繪製圖表",
                color = Slate
            )
        }
        return
    }

    val chartPoints = history.takeLast(30)
    val prices = chartPoints.map { it.price }
    val minPrice = prices.minOrNull() ?: 0.0
    val maxPrice = prices.maxOrNull() ?: 0.0
    val range = (maxPrice - minPrice).takeIf { it > 0.0 } ?: 1.0

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
                val normalized = ((point.price - minPrice) / range).toFloat()
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
                text = chartPoints.first().tradeDate.toDisplayDate(),
                color = Slate,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Low $${String.format(Locale.US, "%.2f", minPrice)}",
                color = Slate,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "High $${String.format(Locale.US, "%.2f", maxPrice)}",
                color = Slate,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = chartPoints.last().tradeDate.toDisplayDate(),
                color = Slate,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RecentPriceRow(point: OilPricePoint) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = point.tradeDate.toDisplayDate(),
                color = Ink,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Slate,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = OilPriceRepository.formatFetchedAt(point.fetchedAt),
                    color = Slate,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Sand
        ) {
            Text(
                text = "$${String.format(Locale.US, "%.2f", point.price)}",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                color = Midnight
            )
        }
    }
}

private fun String.toDisplayDate(): String {
    return runCatching {
        LocalDate.parse(this).format(DateTimeFormatter.ofPattern("MM/dd"))
    }.getOrDefault(this)
}
