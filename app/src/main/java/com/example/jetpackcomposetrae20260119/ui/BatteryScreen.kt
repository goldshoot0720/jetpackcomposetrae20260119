package com.example.jetpackcomposetrae20260119.ui

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
import androidx.compose.foundation.lazy.LazyListScope
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
import com.example.jetpackcomposetrae20260119.data.BatteryRepository
import com.example.jetpackcomposetrae20260119.data.BatterySnapshot
import com.example.jetpackcomposetrae20260119.ui.theme.Copper
import com.example.jetpackcomposetrae20260119.ui.theme.Fog
import com.example.jetpackcomposetrae20260119.ui.theme.Garnet
import com.example.jetpackcomposetrae20260119.ui.theme.Ink
import com.example.jetpackcomposetrae20260119.ui.theme.Midnight
import com.example.jetpackcomposetrae20260119.ui.theme.Porcelain
import com.example.jetpackcomposetrae20260119.ui.theme.Slate

@Composable
fun BatteryScreen(
    viewModel: BatteryViewModel,
    headerContent: LazyListScope.() -> Unit = {}
) {
    val snapshot by viewModel.snapshot.collectAsState()
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
            headerContent()

            snapshot?.let { batterySnapshot ->
                item {
                    BatteryInfoCard(
                        snapshot = batterySnapshot,
                        isLoading = isLoading,
                        onRefresh = viewModel::refresh
                    )
                }
            }

            if (snapshot == null && errorMessage == null) {
                item {
                    BatteryEmptyCard(
                        isLoading = isLoading,
                        onRefresh = viewModel::refresh
                    )
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

        if (isLoading && snapshot == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Copper
            )
        }
    }
}

@Composable
private fun BatteryEmptyCard(
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
                text = "Battery Panel",
                style = MaterialTheme.typography.labelMedium,
                color = Copper
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\u67e5\u770b\u96fb\u6c60\u6700\u8fd1\u4e00\u6b21\u5145\u6eff\u8207\u76ee\u524d\u4f30\u7b97",
                style = MaterialTheme.typography.headlineLarge,
                color = Ink
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "\u5167\u5bb9\u5305\u542b\u4e0a\u6b21\u5145\u6eff\u96fb\u6642\u9593\u3001\u8ddd\u4eca\u6642\u9593\u5dee\u3001\u73fe\u5728\u96fb\u91cf\uff0c\u4ee5\u53ca\u9810\u4f30\u5145\u6eff\u6642\u9593\u8207\u8ddd\u4eca\u5dee\u8ddd\u3002",
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
                Text(text = if (isLoading) "\u91cd\u65b0\u6574\u7406\u4e2d..." else "\u91cd\u65b0\u8b80\u53d6\u96fb\u6c60\u8cc7\u8a0a")
            }
        }
    }
}

@Composable
private fun BatteryInfoCard(
    snapshot: BatterySnapshot,
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
            Text(
                text = "Battery Status",
                style = MaterialTheme.typography.labelMedium,
                color = Copper
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\u96fb\u6c60\u8cc7\u8a0a",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "\u8cc7\u6599\u6703\u96a8\u8457\u91cd\u65b0\u8b80\u53d6\u66f4\u65b0\uff0c\u4e0a\u6b21\u5145\u6eff\u96fb\u6642\u9593\u6703\u5728\u88dd\u7f6e\u5168\u6eff\u6642\u8a18\u9304\u3002",
                style = MaterialTheme.typography.bodySmall,
                color = Fog.copy(alpha = 0.68f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            BatteryInfoRow(
                label = "\u4e0a\u6b21\u5145\u6eff\u96fb\u6642\u9593",
                value = BatteryRepository.formatTimestamp(snapshot.lastFullChargeAt)
            )
            DividerSpacer()
            BatteryInfoRow(
                label = "\u4e0a\u6b21\u5145\u6eff\u96fb\u8ddd\u96e2\u7576\u4e0b\u6642\u9593\u5dee",
                value = BatteryRepository.formatRelativeDifference(snapshot.lastFullChargeAt)
            )
            DividerSpacer()
            BatteryInfoRow(
                label = "\u73fe\u5728\u96fb\u91cf",
                value = "${snapshot.currentLevelPercent}%"
            )
            DividerSpacer()
            BatteryInfoRow(
                label = snapshot.estimatedTargetLabel,
                value = BatteryRepository.formatTimestamp(snapshot.estimatedTargetTimeMillis)
            )
            DividerSpacer()
            BatteryInfoRow(
                label = "${snapshot.estimatedTargetLabel}\u8ddd\u96e2\u7576\u4e0b\u6642\u9593\u5dee",
                value = if (snapshot.isCharging) {
                    BatteryRepository.formatRelativeDifference(snapshot.estimatedTargetTimeMillis)
                } else {
                    "\u672a\u5145\u96fb\uff0c\u7121\u6cd5\u4f30\u7b97"
                }
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
                Text(text = if (isLoading) "\u91cd\u65b0\u6574\u7406\u4e2d..." else "\u91cd\u65b0\u8b80\u53d6\u96fb\u6c60\u8cc7\u8a0a")
            }
        }
    }
}

@Composable
private fun BatteryInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Fog.copy(alpha = 0.72f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DividerSpacer() {
    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = Fog.copy(alpha = 0.18f))
    Spacer(modifier = Modifier.height(12.dp))
}
