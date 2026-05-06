package com.example.jetpackcomposetrae20260119.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.jetpackcomposetrae20260119.ui.theme.Cloud
import com.example.jetpackcomposetrae20260119.ui.theme.Copper
import com.example.jetpackcomposetrae20260119.ui.theme.Fog
import com.example.jetpackcomposetrae20260119.ui.theme.Ink
import com.example.jetpackcomposetrae20260119.ui.theme.Midnight
import com.example.jetpackcomposetrae20260119.ui.theme.Outline
import com.example.jetpackcomposetrae20260119.ui.theme.Porcelain
import com.example.jetpackcomposetrae20260119.ui.theme.Slate
import kotlin.math.max

@Composable
fun PriceComparisonScreen(
    headerContent: LazyListScope.() -> Unit = {}
) {
    var activeTool by rememberSaveable { mutableStateOf(FengTool.GeneralPrice) }

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
                ToolHeroCard()
            }

            item {
                ToolSwitcher(
                    activeTool = activeTool,
                    onToolSelected = { activeTool = it }
                )
            }

            when (activeTool) {
                FengTool.GeneralPrice -> generalPriceItems()
                FengTool.PhonePrice -> phonePriceItems()
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

private fun LazyListScope.generalPriceItems() {
    item {
        GeneralPriceTool()
    }
}

private fun LazyListScope.phonePriceItems() {
    item {
        PhonePriceTool()
    }
}

@Composable
private fun ToolHeroCard() {
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
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFF1D9),
                            Color(0xFFE7F6F2),
                            Porcelain
                        ),
                        start = Offset.Zero,
                        end = Offset(900f, 360f)
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .border(1.dp, Color(0xFFD8D8C8), RoundedCornerShape(30.dp))
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "FENG BRO TOOLBOX",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate
                )
                Text(
                    text = "鋒兄工具",
                    style = MaterialTheme.typography.displayMedium,
                    color = Midnight
                )
                Text(
                    text = "整合鋒兄比價與手機比價，快速查看價格走勢、最低價、通路差價與入手判斷。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Slate
                )
            }
        }
    }
}

@Composable
private fun ToolSwitcher(
    activeTool: FengTool,
    onToolSelected: (FengTool) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = Fog
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FengTool.entries.forEach { tool ->
                Surface(
                    onClick = { onToolSelected(tool) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = if (activeTool == tool) Midnight else Porcelain,
                    border = BorderStroke(1.dp, if (activeTool == tool) Midnight else Outline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (tool == FengTool.PhonePrice) Icons.Default.Info else Icons.Default.Search,
                            contentDescription = tool.title,
                            tint = if (activeTool == tool) Copper else Ink,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = tool.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (activeTool == tool) Fog else Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneralPriceTool() {
    val sampleLinks = remember {
        listOf(
            "https://24h.pchome.com.tw/prod/DRAH5Z-A900FAB00",
            "https://m.momoshop.com.tw/goods.momo?i_code=12345678",
            "https://www.landtop.com.tw/products/apple-iphone-17"
        )
    }
    var currentLink by rememberSaveable { mutableStateOf(sampleLinks.first()) }
    var selectedRange by rememberSaveable { mutableStateOf("五年") }
    val report = remember(currentLink, selectedRange) { sampleReportFor(currentLink, selectedRange) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        InputCard(
            currentLink = currentLink,
            selectedRange = selectedRange,
            sampleLinks = sampleLinks,
            onLinkChanged = { currentLink = it },
            onRangeChanged = { selectedRange = it }
        )
        TrendChartCard(report = report)
        PriceStatsCard(report = report)
        RecentLinksCard(
            links = sampleLinks,
            onSelect = { currentLink = it }
        )
    }
}

@Composable
private fun InputCard(
    currentLink: String,
    selectedRange: String,
    sampleLinks: List<String>,
    onLinkChanged: (String) -> Unit,
    onRangeChanged: (String) -> Unit
) {
    val rangeOptions = listOf("30天", "一年", "五年")

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
            SectionTitle("鋒兄比價", "貼上商品連結，檢視模擬歷史價格與是否適合入手。")
            OutlinedTextField(
                value = currentLink,
                onValueChange = onLinkChanged,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                trailingIcon = {
                    VoiceInputTrailingIcon(
                        fieldLabel = "鋒兄比價商品網址或關鍵字",
                        onConfirmedText = onLinkChanged
                    )
                },
                label = { Text("商品網址") }
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
                        border = BorderStroke(1.dp, if (selected) Copper else Outline)
                    ) {
                        Text(
                            text = option,
                            color = if (selected) Copper else Ink
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE28A2B))
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("重新分析")
                }

                OutlinedButton(
                    onClick = { onLinkChanged(sampleLinks.first()) },
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("載入範例")
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
                text = "${report.startDate} 到 ${report.endDate}，共 ${report.points} 筆價格紀錄",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Fog
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    PriceLineChart(report = report)
                    Text(
                        text = "最新 ${report.latestPriceLabel}  /  最低 ${report.lowestPriceLabel}  /  中位數 ${report.medianPriceLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceLineChart(report: PriceReport) {
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
        val leftPadding = 42f
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

        listOf(0, report.pointsData.lastIndex).forEach { index ->
            val x = leftPadding + (chartWidth * index / max(report.pointsData.lastIndex, 1))
            val normalized = (report.pointsData[index] - minValue) / range
            val y = topPadding + chartHeight - (chartHeight * normalized)
            drawCircle(color = Color.White, radius = 10f, center = Offset(x, y))
            drawCircle(color = lineColor, radius = 6f, center = Offset(x, y))
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
            SectionTitle("價格摘要", report.recommendation)
            val statItems = listOf(
                "最新價格" to report.latestPriceLabel,
                "最低價格" to report.lowestPriceLabel,
                "最高價格" to report.highestPriceLabel,
                "平均價格" to report.averagePriceLabel,
                "中位價格" to report.medianPriceLabel,
                "距離低點" to report.latestLowestGap
            )

            statItems.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { (label, value) ->
                        StatTile(
                            label = label,
                            value = value,
                            modifier = Modifier.weight(1f)
                        )
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
            SectionTitle("最近查詢", "點選範例連結快速切換報表。")
            links.forEach { link ->
                Surface(
                    onClick = { onSelect(link) },
                    shape = RoundedCornerShape(18.dp),
                    color = Fog
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
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

@Composable
private fun PhonePriceTool() {
    var query by rememberSaveable { mutableStateOf("Samsung") }
    val phones = remember(query) {
        phoneSamples
            .filter { phone ->
                val text = "${phone.brand} ${phone.name}".lowercase()
                query.isBlank() || query.lowercase().split(" ").all(text::contains)
            }
            .sortedBy { it.bestPrice ?: Int.MAX_VALUE }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(30.dp),
        color = Porcelain
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle(
                title = "手機比價",
                subtitle = "參考鋒兄工具的地標/傑昇比較模式，搜尋 iPhone、Samsung、Pixel 等機型。"
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                label = { Text("搜尋手機") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    VoiceInputTrailingIcon(
                        fieldLabel = "手機比價搜尋",
                        onConfirmedText = { query = it }
                    )
                }
            )

            PhonePriceChart(phones = phones)

            phones.forEach { phone ->
                PhonePriceCard(phone)
            }

            if (phones.isEmpty()) {
                Text(
                    text = "沒有符合的手機資料，換個關鍵字試試。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate
                )
            }
        }
    }
}

@Composable
private fun PhonePriceChart(phones: List<PhonePrice>) {
    val chartItems = phones.take(5)
    if (chartItems.isEmpty()) return
    val maxPrice = chartItems.maxOf { it.suggestedPrice ?: it.bestPrice ?: 1 }.coerceAtLeast(1)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Fog
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "最低價排行",
                style = MaterialTheme.typography.titleMedium,
                color = Midnight
            )
            chartItems.forEach { phone ->
                val price = phone.bestPrice ?: 0
                val width = (price.toFloat() / maxPrice.toFloat()).coerceIn(0.08f, 1f)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = phone.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatCurrency(price),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Copper,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(Cloud, RoundedCornerShape(20.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(width)
                                .height(10.dp)
                                .background(Color(0xFF2F8F57), RoundedCornerShape(20.dp))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhonePriceCard(phone: PhonePrice) {
    val suggestedPrice = phone.suggestedPrice
    val bestPrice = phone.bestPrice
    val savings = if (suggestedPrice != null && bestPrice != null) {
        suggestedPrice - bestPrice
    } else {
        null
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Fog,
        border = BorderStroke(1.dp, Outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = phone.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Midnight
                    )
                    Text(
                        text = phone.brand,
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate
                    )
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE8F5EE)
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        text = phone.bestStore ?: "待查",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF25724B)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("建議售價", formatCurrency(phone.suggestedPrice), Modifier.weight(1f))
                StatTile("地標", formatCurrency(phone.landtopPrice), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("傑昇", formatCurrency(phone.jyesPrice), Modifier.weight(1f))
                StatTile("可省", savings?.let(::formatCurrency) ?: "--", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Midnight
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate
        )
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.72f)
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
                style = MaterialTheme.typography.titleMedium,
                color = Midnight,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private enum class FengTool(val title: String) {
    GeneralPrice("鋒兄比價"),
    PhonePrice("手機比價")
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
    val latestLowestGap: String,
    val recommendation: String,
    val pointsData: List<Float>
)

private data class PhonePrice(
    val brand: String,
    val name: String,
    val suggestedPrice: Int?,
    val landtopPrice: Int?,
    val jyesPrice: Int?
) {
    val bestPrice: Int?
        get() = listOfNotNull(landtopPrice, jyesPrice).minOrNull()

    val bestStore: String?
        get() = when (bestPrice) {
            null -> null
            landtopPrice -> "地標"
            else -> "傑昇"
        }
}

private fun sampleReportFor(link: String, range: String): PriceReport {
    val baseSeries = when {
        link.contains("landtop", ignoreCase = true) -> listOf(
            44900f, 43800f, 42900f, 42100f, 41500f, 40900f, 39900f, 38900f, 38600f, 37900f
        )
        link.contains("momo", ignoreCase = true) -> listOf(
            11990f, 11890f, 11750f, 11590f, 11490f, 11390f, 11190f, 10990f, 10890f, 10790f,
            10590f, 10490f, 10390f, 10290f, 10190f, 9990f, 9790f, 9690f, 9590f, 9490f
        )
        else -> listOf(
            1790f, 1805f, 1812f, 1798f, 1835f, 1820f, 1848f, 1830f, 1850f, 1840f,
            1888f, 1920f, 2100f, 2600f, 3100f, 3650f, 4470f, 4690f, 4520f, 5690f,
            5480f, 5590f, 5410f, 5560f, 5690f
        )
    }

    val series = when (range) {
        "30天" -> baseSeries.takeLast(8)
        "一年" -> baseSeries.takeLast(14)
        else -> baseSeries
    }
    val latest = series.last().toInt()
    val lowest = series.minOrNull()?.toInt() ?: latest
    val highest = series.maxOrNull()?.toInt() ?: latest
    val average = series.average()
    val sorted = series.sorted()
    val median = sorted[sorted.size / 2].toInt()
    val gap = latest - lowest

    return PriceReport(
        title = when {
            link.contains("landtop", ignoreCase = true) -> "Apple iPhone 17 256GB"
            link.contains("momo", ignoreCase = true) -> "Samsung Galaxy A37 5G 8G/128G"
            else -> "KIOXIA Exceria G2 SSD M.2 1TB"
        },
        startDate = if (range == "30天") "2026-03-25" else if (range == "一年") "2025-04-23" else "2021-04-23",
        endDate = "2026-04-23",
        points = series.size,
        latestPriceLabel = formatCurrency(latest),
        lowestPriceLabel = formatCurrency(lowest),
        highestPriceLabel = formatCurrency(highest),
        averagePriceLabel = formatCurrency(average.toInt()),
        medianPriceLabel = formatCurrency(median),
        latestLowestGap = if (gap == 0) "目前低點" else "+${formatCurrency(gap)}",
        recommendation = if (gap <= latest * 0.05f) "價格接近區間低點，可以列入觀察或準備入手。" else "目前離低點仍有距離，建議等下一波折扣。",
        pointsData = series
    )
}

private fun formatCurrency(value: Int?): String {
    return value?.let { "NT$ ${"%,d".format(it)}" } ?: "--"
}

private val phoneSamples = listOf(
    PhonePrice("Apple", "iPhone 17 256GB", 36900, 31990, 31490),
    PhonePrice("Apple", "iPhone 17 Pro 256GB", 43900, 38990, 38490),
    PhonePrice("Apple", "iPhone 16 128GB", 29900, 24490, 23990),
    PhonePrice("Samsung", "Samsung S26 12G/256GB", 34900, 28990, 28290),
    PhonePrice("Samsung", "Samsung S26 Ultra 12G/256GB", 44900, 37990, 37290),
    PhonePrice("Samsung", "Samsung A17 8G/128GB", 10990, 8490, 8190),
    PhonePrice("Google", "Pixel 10 12G/128GB", 26900, 21990, 21490),
    PhonePrice("ASUS", "ROG Phone 10 16G/512GB", 38990, 33990, 33490)
)
