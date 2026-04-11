package com.example.jetpackcomposetrae20260119.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.jetpackcomposetrae20260119.ui.theme.Copper
import com.example.jetpackcomposetrae20260119.ui.theme.Fog
import com.example.jetpackcomposetrae20260119.ui.theme.Ink
import com.example.jetpackcomposetrae20260119.ui.theme.Midnight
import com.example.jetpackcomposetrae20260119.ui.theme.Outline
import com.example.jetpackcomposetrae20260119.ui.theme.Porcelain
import com.example.jetpackcomposetrae20260119.ui.theme.Slate
import kotlin.math.max
import kotlin.math.min

@Composable
fun PriceComparisonScreen(
    headerContent: LazyListScope.() -> Unit = {}
) {
    val sampleLinks = remember {
        listOf(
            "https://24h.pchome.com.tw/prod/DRAH5Z-A900FAB00",
            "https://m.momoshop.com.tw/goods.momo?i_code=12345678",
            "https://24h.pchome.com.tw/prod/DYAR12-A900ZZ999"
        )
    }
    var currentLink by remember { mutableStateOf(sampleLinks.first()) }
    var selectedRange by remember { mutableStateOf("最長5年") }
    val report = remember(currentLink) { sampleReportFor(currentLink) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            headerContent()

            item {
                PriceHeroCard()
            }

            item {
                ControlPanelCard(
                    currentLink = currentLink,
                    selectedRange = selectedRange,
                    onLinkChanged = { currentLink = it },
                    onRangeChanged = { selectedRange = it }
                )
            }

            item {
                TrendChartCard(report = report)
            }

            item {
                PriceStatsCard(report = report)
            }

            item {
                RecentLinksCard(
                    links = sampleLinks,
                    onSelect = { currentLink = it }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun PriceHeroCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFE8C8),
                            Porcelain
                        ),
                        radius = 780f,
                        center = Offset(820f, 420f)
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .border(1.dp, Color(0xFFE2D2BF), RoundedCornerShape(30.dp))
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "IN-BROWSER PRICE READER",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate
                )
                Text(
                    text = "鋒兄比價",
                    style = MaterialTheme.typography.displayMedium,
                    color = Midnight
                )
                Text(
                    text = "貼上 PChome 或 momo 商品連結後，整理價格走勢與統計資訊，讓首頁直接變成鋒兄的價格觀測台。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Slate
                )
            }
        }
    }
}

@Composable
private fun ControlPanelCard(
    currentLink: String,
    selectedRange: String,
    onLinkChanged: (String) -> Unit,
    onRangeChanged: (String) -> Unit
) {
    val rangeOptions = listOf("最近90天", "最近1年", "最長5年")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(30.dp),
        color = Porcelain
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "商品連結",
                style = MaterialTheme.typography.labelLarge,
                color = Ink
            )
            OutlinedTextField(
                value = currentLink,
                onValueChange = onLinkChanged,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                singleLine = true
            )

            Text(
                text = "查詢天數",
                style = MaterialTheme.typography.labelLarge,
                color = Ink
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rangeOptions.forEach { option ->
                    val selected = option == selectedRange
                    OutlinedButton(
                        onClick = { onRangeChanged(option) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) Color(0xFFFFE4BF) else Fog
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selected) Copper else Outline
                        )
                    ) {
                        Text(
                            text = option,
                            color = if (selected) Copper else Ink
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE28A2B))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("產生圖表")
                }

                OutlinedButton(
                    onClick = {},
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("貼上剪貼簿")
                }

                OutlinedButton(
                    onClick = { onLinkChanged("") },
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("清空")
                }
            }
        }
    }
}

@Composable
private fun TrendChartCard(report: PriceReport) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(30.dp),
        color = Porcelain
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = report.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Midnight
            )
            Text(
                text = "${report.startDate} to ${report.endDate}, ${report.points} points inside the last-five-years window",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Fog
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        val lineColor = Color(0xFFE27628)
                        val gridColor = Color(0xFFD9D2C7)
                        val minValue = report.pointsData.minOrNull() ?: 0f
                        val maxValue = report.pointsData.maxOrNull() ?: 1f
                        val range = max(maxValue - minValue, 1f)

                        val leftPadding = 70f
                        val topPadding = 24f
                        val bottomPadding = 28f
                        val chartWidth = size.width - leftPadding - 10f
                        val chartHeight = size.height - topPadding - bottomPadding

                        repeat(5) { index ->
                            val y = topPadding + chartHeight * index / 4f
                            drawLine(
                                color = gridColor,
                                start = Offset(leftPadding, y),
                                end = Offset(leftPadding + chartWidth, y),
                                strokeWidth = 1f
                            )
                        }

                        repeat(4) { index ->
                            val x = leftPadding + chartWidth * index / 3f
                            drawLine(
                                color = gridColor.copy(alpha = 0.8f),
                                start = Offset(x, topPadding),
                                end = Offset(x, topPadding + chartHeight),
                                strokeWidth = 1f
                            )
                        }

                        val path = Path()
                        report.pointsData.forEachIndexed { index, value ->
                            val x = leftPadding + (chartWidth * index / max(report.pointsData.lastIndex, 1))
                            val normalized = (value - minValue) / range
                            val y = topPadding + chartHeight - (chartHeight * normalized)
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 5f, cap = StrokeCap.Round)
                        )

                        val labeledPoints = listOf(
                            report.pointsData.indexOf(report.lowestPrice.toFloat()) to "最低 ${report.lowestPriceLabel}",
                            report.pointsData.indexOf(report.medianPrice.toFloat()) to "中位數 ${report.medianPriceLabel}",
                            report.pointsData.lastIndex to "最新 ${report.latestPriceLabel}"
                        ).filter { it.first >= 0 }

                        labeledPoints.forEachIndexed { markerIndex, pair ->
                            val index = pair.first
                            val x = leftPadding + (chartWidth * index / max(report.pointsData.lastIndex, 1))
                            val normalized = (report.pointsData[index] - minValue) / range
                            val y = topPadding + chartHeight - (chartHeight * normalized)
                            drawCircle(
                                color = if (markerIndex == 1) Color(0xFF7D4DB7) else if (markerIndex == 0) Color(0xFF2F8F57) else lineColor,
                                radius = 8f,
                                center = Offset(x, y)
                            )
                        }
                    }

                    Text(
                        text = "最新 ${report.latestPriceLabel}  •  最低 ${report.lowestPriceLabel}  •  中位數 ${report.medianPriceLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceStatsCard(report: PriceReport) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(30.dp),
        color = Porcelain
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "價格統計",
                style = MaterialTheme.typography.headlineSmall,
                color = Midnight
            )

            val statItems = listOf(
                "最新價格" to report.latestPriceLabel,
                "最低價格" to report.lowestPriceLabel,
                "最高價格" to report.highestPriceLabel,
                "平均價格" to report.averagePriceLabel,
                "中位數價格" to report.medianPriceLabel,
                "最新減去最低價格" to report.deltaPriceLabel,
                "最新與最低間隔" to report.latestLowestGap
            )

            val chunks = statItems.chunked(2)
            chunks.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { (label, value) ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            color = Fog
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate
                                )
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Midnight,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentLinksCard(
    links: List<String>,
    onSelect: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(30.dp),
        color = Porcelain
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "最近連結",
                style = MaterialTheme.typography.headlineSmall,
                color = Midnight
            )

            links.forEach { link ->
                Surface(
                    onClick = { onSelect(link) },
                    shape = RoundedCornerShape(18.dp),
                    color = Fog
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text(
                            text = link,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private data class PriceReport(
    val title: String,
    val startDate: String,
    val endDate: String,
    val points: Int,
    val latestPriceLabel: String,
    val lowestPriceLabel: String,
    val highestPriceLabel: String,
    val averagePriceLabel: String,
    val medianPriceLabel: String,
    val deltaPriceLabel: String,
    val latestLowestGap: String,
    val lowestPrice: Int,
    val medianPrice: Int,
    val pointsData: List<Float>
)

private fun sampleReportFor(link: String): PriceReport {
    val title = when {
        link.contains("pchome", ignoreCase = true) -> "KIOXIA 鎧俠 Exceria G2 SSD M.2 2280 PCIe NVMe 1TB Gen3x4"
        link.contains("momo", ignoreCase = true) -> "Samsung Galaxy A37 5G (8G/128G)"
        else -> "MSI MPG A850G PCIe5 電源供應器"
    }

    val series = when {
        link.contains("pchome", ignoreCase = true) -> listOf(
            1790f, 1805f, 1812f, 1798f, 1835f, 1820f, 1848f, 1830f, 1850f, 1840f,
            1888f, 1920f, 2100f, 2600f, 3100f, 3650f, 4470f, 4690f, 4520f, 5690f,
            5480f, 5590f, 5410f, 5560f, 5690f
        )
        link.contains("momo", ignoreCase = true) -> listOf(
            11990f, 11890f, 11750f, 11590f, 11490f, 11390f, 11190f, 10990f, 10890f, 10790f,
            10590f, 10490f, 10390f, 10290f, 10190f, 9990f, 9790f, 9690f, 9590f, 9490f
        )
        else -> listOf(
            5290f, 5190f, 5090f, 4990f, 4890f, 4750f, 4650f, 4550f, 4490f, 4390f,
            4450f, 4590f, 4690f, 4790f, 4890f, 4950f, 5090f
        )
    }

    val latest = series.last().toInt()
    val lowest = series.minOrNull()?.toInt() ?: latest
    val highest = series.maxOrNull()?.toInt() ?: latest
    val average = series.average()
    val sorted = series.sorted()
    val median = sorted[sorted.size / 2].toInt()

    return PriceReport(
        title = title,
        startDate = if (series.size > 20) "2025-03-19" else "2025-07-08",
        endDate = "2026-04-11",
        points = series.size,
        latestPriceLabel = "$${formatNumber(latest)}",
        lowestPriceLabel = "$${formatNumber(lowest)}",
        highestPriceLabel = "$${formatNumber(highest)}",
        averagePriceLabel = formatDecimal(average),
        medianPriceLabel = formatDecimal(median.toDouble()),
        deltaPriceLabel = "$${formatNumber(latest - lowest)}",
        latestLowestGap = if (series.size > 20) "1 年 0 個月 23 天" else "0 年 6 個月 11 天",
        lowestPrice = lowest,
        medianPrice = median,
        pointsData = series
    )
}

private fun formatNumber(value: Int): String {
    return "%,d".format(value)
}

private fun formatDecimal(value: Double): String {
    return "%,.2f".format(value)
}
