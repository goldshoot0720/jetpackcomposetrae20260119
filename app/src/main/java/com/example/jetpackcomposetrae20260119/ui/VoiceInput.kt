package com.example.jetpackcomposetrae20260119.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Locale

@Composable
fun VoiceInputTrailingIcon(
    fieldLabel: String,
    onConfirmedText: (String) -> Unit
) {
    VoiceInputLauncher(
        fieldLabel = fieldLabel,
        onConfirmedText = onConfirmedText
    ) { startListening ->
        IconButton(onClick = startListening) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "$fieldLabel 語音輸入"
            )
        }
    }
}

@Composable
fun VoiceInputActionButton(
    label: String,
    fieldLabel: String,
    onConfirmedText: (String) -> Unit
) {
    VoiceInputLauncher(
        fieldLabel = fieldLabel,
        onConfirmedText = onConfirmedText
    ) { startListening ->
        OutlinedButton(onClick = startListening) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null
            )
            Text(label)
        }
    }
}

@Composable
private fun VoiceInputLauncher(
    fieldLabel: String,
    onConfirmedText: (String) -> Unit,
    content: @Composable (startListening: () -> Unit) -> Unit
) {
    var pendingText by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            return@rememberLauncherForActivityResult
        }

        val matches = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            .orEmpty()
        val recognized = matches.firstOrNull()?.trim().orEmpty()
        if (recognized.isNotBlank()) {
            pendingText = recognized
        }
    }

    val startListening = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.TAIWAN.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "請說出$fieldLabel")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }

        try {
            launcher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            errorText = "這台裝置找不到可用的語音辨識服務。"
        }
    }

    content(startListening)

    pendingText?.let { text ->
        AlertDialog(
            onDismissRequest = { pendingText = null },
            title = { Text("套用語音輸入？") },
            text = { Text(text) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmedText(text)
                        pendingText = null
                    }
                ) {
                    Text("套用")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingText = null }) {
                    Text("取消")
                }
            }
        )
    }

    errorText?.let { text ->
        AlertDialog(
            onDismissRequest = { errorText = null },
            title = { Text("語音輸入不可用") },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = { errorText = null }) {
                    Text("知道了")
                }
            }
        )
    }
}
