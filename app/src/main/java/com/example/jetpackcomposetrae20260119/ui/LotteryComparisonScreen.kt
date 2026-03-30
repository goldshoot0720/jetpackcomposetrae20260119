package com.example.jetpackcomposetrae20260119.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jetpackcomposetrae20260119.data.LotteryDraw
import com.example.jetpackcomposetrae20260119.data.LotterySection
import com.example.jetpackcomposetrae20260119.data.LotteryTicket
import com.example.jetpackcomposetrae20260119.ui.theme.Copper
import com.example.jetpackcomposetrae20260119.ui.theme.Fog
import com.example.jetpackcomposetrae20260119.ui.theme.Garnet
import com.example.jetpackcomposetrae20260119.ui.theme.Ink
import com.example.jetpackcomposetrae20260119.ui.theme.Midnight
import com.example.jetpackcomposetrae20260119.ui.theme.Porcelain
import com.example.jetpackcomposetrae20260119.ui.theme.Slate

@Composable
fun LotteryComparisonScreen(viewModel: LotteryComparisonViewModel) {
    val sections by viewModel.sections.collectAsState()
    val rangeLabel by viewModel.rangeLabel.collectAsState()
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
                LotterySourceCard(
                    rangeLabel = rangeLabel,
                    isLoading = isLoading,
                    onRefresh = viewModel::refresh
                )
            }

            sections.forEach { section ->
                item(section.id) {
                    LotterySectionCard(section = section)
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

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (isLoading && sections.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Copper
            )
        }
    }
}

@Composable
private fun LotterySourceCard(
    rangeLabel: String,
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
                text = "\u6700\u778e\u7d50\u5a5a\u7406\u7531",
                style = MaterialTheme.typography.labelMedium,
                color = Copper
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\u53f0\u5f69\u5404\u671f\u865f\u78bc\u8207\u81ea\u8a02\u7d44\u5408\u6bd4\u5c0d",
                style = MaterialTheme.typography.headlineLarge,
                color = Ink
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "\u8cc7\u6599\u4f86\u6e90\u4f9d\u7167\u53f0\u7063\u5f69\u5238\u5b98\u65b9\u7d50\u679c\u9801\uff0c\u6574\u7406\u5a01\u529b\u5f69\u3001\u5927\u6a02\u900f\u3001\u4eca\u5f69539\u8fd1\u4e09\u500b\u6708\u5404\u671f\u865f\u78bc\uff0c\u4e26\u9010\u671f\u6bd4\u5c0d\u6307\u5b9a\u7d44\u5408\u3002",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate
            )
            if (rangeLabel.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = rangeLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = Midnight.copy(alpha = 0.68f)
                )
            }
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
                Text(text = if (isLoading) "\u66f4\u65b0\u4e2d..." else "\u91cd\u65b0\u6293\u53d6\u5b98\u65b9\u8cc7\u6599")
            }
        }
    }
}

@Composable
private fun LotterySectionCard(section: LotterySection) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(30.dp),
        color = Porcelain
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleLarge,
                color = Ink
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\u4f86\u6e90\uff1a${section.sourceUrl}",
                style = MaterialTheme.typography.bodySmall,
                color = Slate
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\u5171 ${section.draws.size} \u671f",
                style = MaterialTheme.typography.labelLarge,
                color = Midnight.copy(alpha = 0.68f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            section.draws.forEachIndexed { index, draw ->
                DrawCard(draw = draw, tickets = section.tickets)
                if (index != section.draws.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun DrawCard(
    draw: LotteryDraw,
    tickets: List<LotteryTicket>
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Fog
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "\u7b2c ${draw.period} \u671f",
                style = MaterialTheme.typography.titleMedium,
                color = Ink
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = draw.lotteryDate,
                style = MaterialTheme.typography.bodySmall,
                color = Slate
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                draw.numbers.forEach { number ->
                    NumberBall(number = number, emphasized = false)
                }
                draw.specialNumber?.let { special ->
                    NumberBall(number = special, emphasized = true)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFE4DDD1))
            Spacer(modifier = Modifier.height(14.dp))

            tickets.forEachIndexed { index, ticket ->
                TicketComparisonRow(
                    ticket = ticket,
                    draw = draw
                )
                if (index != tickets.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun TicketComparisonRow(
    ticket: LotteryTicket,
    draw: LotteryDraw
) {
    val matchedNumbers = ticket.numbers.intersect(draw.numbers.toSet()).sorted()
    val specialMatched = ticket.specialNumber != null && draw.specialNumber == ticket.specialNumber
    val exactMatch = matchedNumbers.size == ticket.numbers.size &&
        (ticket.specialNumber == null || specialMatched)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${ticket.label}\uff1a${ticket.numbers.joinToString(" ") { it.toTwoDigits() }}" +
                    if (ticket.specialNumber != null) " \u7279\u5225\u865f ${ticket.specialNumber.toTwoDigits()}" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildComparisonText(
                    matchedNumbers = matchedNumbers,
                    specialMatched = specialMatched,
                    hasSpecial = ticket.specialNumber != null
                ),
                style = MaterialTheme.typography.bodySmall,
                color = Slate
            )
        }

        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (exactMatch) Copper else Color(0xFFE9E2D5)
        ) {
            Text(
                text = if (exactMatch) "\u5b8c\u5168\u547d\u4e2d" else "\u5c0d\u4e2d ${matchedNumbers.size}" +
                    if (ticket.specialNumber != null && specialMatched) "+1" else "",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = if (exactMatch) Color.White else Midnight,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun buildComparisonText(
    matchedNumbers: List<Int>,
    specialMatched: Boolean,
    hasSpecial: Boolean
): String {
    val matchedText = if (matchedNumbers.isEmpty()) {
        "\u4e3b\u865f\u672a\u5c0d\u4e2d"
    } else {
        "\u4e3b\u865f\u5c0d\u4e2d ${matchedNumbers.joinToString(" ") { it.toTwoDigits() }}"
    }

    return if (hasSpecial) {
        "$matchedText\uff1b\u7279\u5225\u865f${if (specialMatched) "\u6709\u5c0d\u4e2d" else "\u672a\u5c0d\u4e2d"}"
    } else {
        matchedText
    }
}

@Composable
private fun NumberBall(
    number: Int,
    emphasized: Boolean
) {
    Surface(
        shape = CircleShape,
        color = if (emphasized) Copper else Midnight,
        modifier = Modifier.size(38.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = number.toTwoDigits(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun Int.toTwoDigits(): String = toString().padStart(2, '0')
