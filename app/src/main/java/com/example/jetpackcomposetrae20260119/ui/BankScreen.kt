package com.example.jetpackcomposetrae20260119.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun BankScreen(
    headerContent: LazyListScope.() -> Unit = {}
) {
    val bankAccounts = remember { taiwanBankAccounts() }
    val electronicTickets = remember { electronicTicketAccounts() }
    val bankAssetTotal = bankAccounts.sumOf { it.assetAmount }
    val electronicTicketAssetTotal = electronicTickets.sumOf { it.assetAmount }
    val allAssetTotal = bankAssetTotal + electronicTicketAssetTotal

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        headerContent()

        item {
            BankHeroCard(
                allAssetTotal = allAssetTotal,
                bankAssetTotal = bankAssetTotal,
                electronicTicketAssetTotal = electronicTicketAssetTotal
            )
        }

        item {
            BankRuleCard()
        }

        item {
            AccountGroupCard(
                title = "銀行帳戶",
                smallLabel = "台灣的銀行才是銀行喔！",
                totalLabel = "銀行總資產",
                totalValue = bankAssetTotal,
                accounts = bankAccounts
            )
        }

        item {
            AccountGroupCard(
                title = "電子票證",
                smallLabel = "銀行以外的先歸類為電子票證喔！",
                totalLabel = "電子票證總資產",
                totalValue = electronicTicketAssetTotal,
                accounts = electronicTickets
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun BankHeroCard(
    allAssetTotal: Long,
    bankAssetTotal: Long,
    electronicTicketAssetTotal: Long
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFEAF7F2),
                            Color(0xFFFFF6DD),
                            Porcelain
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "鋒兄銀行\n(+電子票證)",
                style = MaterialTheme.typography.headlineMedium,
                color = Midnight
            )
            Text(
                text = "電子票證",
                style = MaterialTheme.typography.labelLarge,
                color = Copper
            )
            Text(
                text = "台灣的銀行才是銀行喔！銀行以外的先歸類為電子票證喔！",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AssetTile("所有資產", allAssetTotal, Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AssetTile("銀行總資產", bankAssetTotal, Modifier.weight(1f))
                    AssetTile("電子票證總資產", electronicTicketAssetTotal, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun BankRuleCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Fog,
        border = BorderStroke(1.dp, Outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "分類規則",
                style = MaterialTheme.typography.titleMedium,
                color = Midnight
            )
            Text(
                text = "1. 所有資產是銀行總資產加上電子票證總資產",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink
            )
            Text(
                text = "2. 中華郵政也屬於台灣銀行",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink
            )
            Text(
                text = "3. 銀行以外的先歸類為電子票證",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink
            )
        }
    }
}

@Composable
private fun AccountGroupCard(
    title: String,
    smallLabel: String,
    totalLabel: String,
    totalValue: Long,
    accounts: List<BankAccountItem>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Porcelain,
        border = BorderStroke(1.dp, Outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Midnight
                    )
                    Text(
                        text = smallLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate
                    )
                }
                AssetTile(totalLabel, totalValue, Modifier.weight(1f))
            }

            accounts.forEach { account ->
                AccountRow(account)
            }
        }
    }
}

@Composable
private fun AssetTile(
    label: String,
    value: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.74f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Slate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatAsset(value),
                style = MaterialTheme.typography.headlineSmall,
                color = Midnight,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AccountRow(account: BankAccountItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Fog
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = account.name,
                style = MaterialTheme.typography.titleSmall,
                color = Ink
            )
            Text(
                text = account.note,
                style = MaterialTheme.typography.bodySmall,
                color = Slate
            )
            Text(
                text = "資產 ${formatAsset(account.assetAmount)}",
                style = MaterialTheme.typography.bodySmall,
                color = Copper
            )
        }
    }
}

private data class BankAccountItem(
    val name: String,
    val note: String,
    val assetAmount: Long = 0L
)

private fun taiwanBankAccounts(): List<BankAccountItem> {
    return listOf(
        BankAccountItem("台灣銀行", "銀行帳戶"),
        BankAccountItem("土地銀行", "銀行帳戶"),
        BankAccountItem("合作金庫", "銀行帳戶"),
        BankAccountItem("第一銀行", "銀行帳戶"),
        BankAccountItem("華南銀行", "銀行帳戶"),
        BankAccountItem("彰化銀行", "銀行帳戶"),
        BankAccountItem("兆豐銀行", "銀行帳戶"),
        BankAccountItem("中華郵政", "銀行帳戶"),
        BankAccountItem("國泰世華銀行", "銀行帳戶"),
        BankAccountItem("玉山銀行", "銀行帳戶"),
        BankAccountItem("台新銀行", "銀行帳戶"),
        BankAccountItem("中國信託銀行", "銀行帳戶"),
        BankAccountItem("永豐銀行", "銀行帳戶")
    )
}

private fun electronicTicketAccounts(): List<BankAccountItem> {
    return listOf(
        BankAccountItem("悠遊卡", "電子票證"),
        BankAccountItem("一卡通", "電子票證"),
        BankAccountItem("icash 2.0", "電子票證"),
        BankAccountItem("愛金卡", "電子票證"),
        BankAccountItem("LINE Pay Money", "電子票證"),
        BankAccountItem("街口支付", "電子票證"),
        BankAccountItem("Pi 拍錢包", "電子票證"),
        BankAccountItem("全支付", "電子票證")
    )
}

private fun formatAsset(value: Long): String {
    val text = value.toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
    return "NT$$text"
}
