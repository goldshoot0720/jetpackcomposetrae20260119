package com.example.jetpackcomposetrae20260119.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jetpackcomposetrae20260119.data.Subscription
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscriptions") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                if (subscriptions.isEmpty()) {
                    Text(
                        text = "No subscriptions found",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(subscriptions) { subscription ->
                            SubscriptionItem(subscription)
                        }
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddSubscriptionDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, price, date, site, note, account ->
                    viewModel.addSubscription(name, price, date, site, note, account)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var site by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(java.time.LocalDate.now().plusMonths(1).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Subscription") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Next Date (YYYY-MM-DD)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = site,
                    onValueChange = { site = it },
                    label = { Text("Site (URL)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("Account") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceInt = price.toIntOrNull() ?: 0
                    // Append time to date to make it ISO-like if it's just a date
                    val fullDate = if (date.contains("T")) date else "${date}T00:00:00.000+00:00"
                    onConfirm(name, priceInt, fullDate, site, note, account)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SubscriptionItem(subscription: Subscription) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${subscription.price}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            val formattedDate = try {
                ZonedDateTime.parse(subscription.nextDate)
                    .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
            } catch (e: Exception) {
                subscription.nextDate
            }
            
            Text(
                text = "Next payment: $formattedDate",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (subscription.account.isNotEmpty()) {
                Text(
                    text = "Account: ${subscription.account}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (subscription.site.isNotEmpty()) {
                Text(
                    text = subscription.site,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            if (subscription.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${subscription.note}",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}
