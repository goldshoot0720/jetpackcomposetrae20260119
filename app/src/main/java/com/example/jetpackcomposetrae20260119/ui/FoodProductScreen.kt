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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
fun FoodProductScreen(
    headerContent: LazyListScope.() -> Unit = {}
) {
    val items = remember { mutableStateListOf<FoodProductItem>() }
    var name by rememberSaveable { mutableStateOf("") }
    var kind by rememberSaveable { mutableStateOf("食品") }
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        headerContent()

        item {
            FoodProductHeroCard(items.size)
        }

        item {
            AddFoodProductCard(
                name = name,
                kind = kind,
                amount = amount,
                note = note,
                onNameChanged = { name = it },
                onKindChanged = { kind = it },
                onAmountChanged = { amount = it },
                onNoteChanged = { note = it },
                onAdd = {
                    val cleanName = name.trim()
                    if (cleanName.isNotBlank()) {
                        items.add(
                            FoodProductItem(
                                name = cleanName,
                                kind = kind.ifBlank { "食品" },
                                amount = amount.ifBlank { "未填數量" },
                                note = note.ifBlank { "尚未備註" }
                            )
                        )
                        name = ""
                        kind = "食品"
                        amount = ""
                        note = ""
                    }
                }
            )
        }

        item {
            FoodProductListCard(items)
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FoodProductHeroCard(totalItems: Int) {
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
                            Color(0xFFFFF4D8),
                            Color(0xFFE9F7EF),
                            Porcelain
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "鋒兄食品\n(+商品)",
                style = MaterialTheme.typography.headlineMedium,
                color = Midnight
            )
            Text(
                text = "新增食品(或商品)",
                style = MaterialTheme.typography.labelLarge,
                color = Copper
            )
            Text(
                text = "只保留新增與查看，列表維持乾淨。",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate
            )
            StatPill("食品/商品總數", totalItems)
        }
    }
}

@Composable
private fun AddFoodProductCard(
    name: String,
    kind: String,
    amount: String,
    note: String,
    onNameChanged: (String) -> Unit,
    onKindChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onAdd: () -> Unit
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
            Text(
                text = "新增食品(或商品)",
                style = MaterialTheme.typography.titleMedium,
                color = Midnight
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                label = { Text("名稱") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = kind,
                    onValueChange = onKindChanged,
                    label = { Text("類別") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChanged,
                    label = { Text("數量") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChanged,
                label = { Text("備註") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
            ) {
                Text("新增食品(或商品)")
            }
        }
    }
}

@Composable
private fun FoodProductListCard(items: List<FoodProductItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Fog,
        border = BorderStroke(1.dp, Outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "食品/商品清單",
                style = MaterialTheme.typography.titleMedium,
                color = Midnight
            )

            if (items.isEmpty()) {
                Text(
                    text = "目前尚未新增食品或商品。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate
                )
            } else {
                items.forEach { item ->
                    FoodProductRow(item)
                }
            }
        }
    }
}

@Composable
private fun FoodProductRow(item: FoodProductItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.74f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleSmall,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${item.kind}｜${item.amount}",
                style = MaterialTheme.typography.bodySmall,
                color = Copper
            )
            Text(
                text = item.note,
                style = MaterialTheme.typography.bodySmall,
                color = Slate
            )
        }
    }
}

@Composable
private fun StatPill(label: String, value: Int) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.74f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Slate
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = Midnight,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class FoodProductItem(
    val name: String,
    val kind: String,
    val amount: String,
    val note: String
)
