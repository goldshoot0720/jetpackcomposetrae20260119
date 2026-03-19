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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.sp
import com.example.jetpackcomposetrae20260119.data.OilPricePoint
import com.example.jetpackcomposetrae20260119.data.OilPriceRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val OilBg = Color(0xFFF7F3EA)
private val OilHeader = Color(0xFF213547)
private val OilPrimary = Color(0xFFB45309)
private val OilPrimarySoft = Color(0xFFFDE7C7)
private val OilAccent = Color(0xFF0F766E)
private val OilCard = Color(0xFFFFFCF7)
private val OilMuted = Color(0xFF6B7280)
private val OilLine = Color(0xFFDC2626)

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
            .background(OilBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1F2937), Color(0xFF374151))
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "石油监控",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "追踪 Gulf Mercantile Exchange 的 OQD Daily Marker Price",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = OilCard)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "最新 OQD Daily Marker Price",
                                    color = OilMuted,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = latest?.let { "$${String.format(Locale.US, "%.2f", it.price)}" } ?: "--",
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Black,
                                    color = OilHeader
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = OilPrimarySoft,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = OilPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        PriceMetaRow(
                            label = "报价日期",
                            value = latest?.displayDate ?: "--"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PriceMetaRow(
                            label = "最近抓取",
                            value = latest?.let { OilPriceRepository.formatFetchedAt(it.fetchedAt) } ?: "--"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PriceMetaRow(
                            label = "资料笔数",
                            value = history.size.toString()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = viewModel::refreshLatestPrice,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(text = if (isLoading) "抓取中..." else "立即更新")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "价格走势",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OilHeader
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "开 App 会自动抓取，每天下午 1:00 也会背景更新。",
                            fontSize = 13.sp,
                            color = OilMuted
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        OilPriceChart(history = history)
                    }
                }
            }

            if (errorMessage != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2))
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFFB91C1C)
                        )
                    }
                }
            }

            if (history.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "最近纪录",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = OilHeader
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            history.takeLast(8).reversed().forEach { point ->
                                RecentPriceRow(point)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (isLoading && history.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = OilPrimary
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
        Text(text = label, color = OilMuted, fontSize = 13.sp)
        Text(text = value, color = OilHeader, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
            Text(text = "还没有油价资料", color = OilMuted)
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
                color = OilPrimarySoft.copy(alpha = 0.4f),
                topLeft = Offset(leftPadding, topPadding),
                size = Size(chartWidth, chartHeight),
                cornerRadius = CornerRadius(24f, 24f)
            )

            repeat(4) { index ->
                val y = topPadding + chartHeight * (index / 3f)
                drawLine(
                    color = Color(0xFFE5E7EB),
                    start = Offset(leftPadding, y),
                    end = Offset(leftPadding + chartWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val offsets = chartPoints.mapIndexed { index, point ->
                val x = if (chartPoints.size == 1) {
                    leftPadding + chartWidth / 2f
                } else {
                    leftPadding + chartWidth * index / (chartPoints.lastIndex.toFloat())
                }
                val normalized = ((point.price - minPrice) / range).toFloat()
                val y = topPadding + chartHeight - (chartHeight * normalized)
                Offset(x, y)
            }

            val fillPath = Path().apply {
                val first = offsets.first()
                moveTo(first.x, topPadding + chartHeight)
                offsets.forEachIndexed { index, offset ->
                    if (index == 0) {
                        lineTo(offset.x, offset.y)
                    } else {
                        lineTo(offset.x, offset.y)
                    }
                }
                val last = offsets.last()
                lineTo(last.x, topPadding + chartHeight)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(OilLine.copy(alpha = 0.25f), Color.Transparent)
                )
            )

            val linePath = Path().apply {
                offsets.forEachIndexed { index, offset ->
                    if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                }
            }

            drawPath(
                path = linePath,
                color = OilLine,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            offsets.forEach { offset ->
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = offset
                )
                drawCircle(
                    color = OilAccent,
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
                color = OilMuted,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Low $${String.format(Locale.US, "%.2f", minPrice)}",
                color = OilMuted,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "High $${String.format(Locale.US, "%.2f", maxPrice)}",
                color = OilMuted,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = chartPoints.last().tradeDate.toDisplayDate(),
                color = OilMuted,
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
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = point.tradeDate.toDisplayDate(),
                color = OilHeader,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "抓取时间 ${OilPriceRepository.formatFetchedAt(point.fetchedAt)}",
                color = OilMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = OilPrimarySoft
        ) {
            Text(
                text = "$${String.format(Locale.US, "%.2f", point.price)}",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold,
                color = OilPrimary
            )
        }
    }
}

private fun String.toDisplayDate(): String {
    return runCatching {
        LocalDate.parse(this).format(DateTimeFormatter.ofPattern("MM/dd"))
    }.getOrDefault(this)
}
