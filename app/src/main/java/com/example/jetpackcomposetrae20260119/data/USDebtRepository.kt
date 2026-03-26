package com.example.jetpackcomposetrae20260119.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class USDebtRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun refreshNationalDebt(): USDebtPoint? = withContext(Dispatchers.IO) {
        runCatching {
            val html = fetchHomepageHtml()
            val point = parseNationalDebt(html)
            if (point != null) {
                savePoint(point)
            }
            point
        }.onFailure { error ->
            Log.e(TAG, "Failed to refresh US debt", error)
        }.getOrNull()
    }

    fun getSavedHistory(): List<USDebtPoint> {
        val raw = prefs.getString(HISTORY_KEY, null).orEmpty()
        if (raw.isBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(
                        USDebtPoint(
                            capturedAt = item.optString("capturedAt"),
                            debt = item.optDouble("debt")
                        )
                    )
                }
            }.sortedBy { it.capturedAt }
        }.getOrElse {
            Log.e(TAG, "Failed to read US debt history", it)
            emptyList()
        }
    }

    private fun fetchHomepageHtml(): String {
        val connection = URL(Constants.US_DEBT_URL).openConnection() as HttpURLConnection
        return connection.run {
            requestMethod = "GET"
            connectTimeout = 20_000
            readTimeout = 20_000
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Mobile Safari/537.36"
            )
            inputStream.bufferedReader().use { it.readText() }
        }
    }

    private fun parseNationalDebt(html: String): USDebtPoint? {
        val elementId = Regex("""<div id="layer29"><span id="([A-Za-z0-9]+)">""")
            .find(html)
            ?.groupValues
            ?.getOrNull(1)
            ?: return null

        val marker = "document.getElementById('$elementId')"
        val markerIndex = html.indexOf(marker)
        if (markerIndex == -1) {
            Log.w(TAG, "Unable to find script marker for $elementId")
            return null
        }

        val searchStart = (markerIndex - 1200).coerceAtLeast(0)
        val snippet = html.substring(searchStart, markerIndex + marker.length)

        val baseValue = Regex("""var\s+$elementId\s*=\s*([0-9]+(?:\.[0-9]+)?)\s*;""")
            .find(snippet)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()
            ?: return null

        val ratePerSecond = Regex("""var\s+R3a45G7S\s*=\s*([0-9]+(?:\.[0-9]+)?)""")
            .find(snippet)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()
            ?: return null

        val anchorSeconds = Regex("""var\s+Y12[a-zA-Z0-9]*\s*=\s*([0-9]+(?:\.[0-9]+)?)""")
            .find(snippet)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()
            ?: return null

        val now = Instant.now()
        val debt = baseValue + (now.epochSecond - anchorSeconds) * ratePerSecond

        return USDebtPoint(
            capturedAt = now.toString(),
            debt = debt
        )
    }

    private fun savePoint(point: USDebtPoint) {
        val merged = (getSavedHistory() + point)
            .sortedBy { it.capturedAt }
            .takeLast(MAX_HISTORY_SIZE)

        val array = JSONArray()
        merged.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("capturedAt", item.capturedAt)
                    put("debt", item.debt)
                }
            )
        }

        prefs.edit().putString(HISTORY_KEY, array.toString()).apply()
    }

    companion object {
        private const val TAG = "USDebtRepository"
        private const val PREFS_NAME = "us_debt_monitor"
        private const val HISTORY_KEY = "us_national_debt_history"
        private const val MAX_HISTORY_SIZE = 180

        fun formatCapturedAt(isoInstant: String): String {
            return runCatching {
                val instant = Instant.parse(isoInstant)
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                    .format(instant.atZone(ZoneId.systemDefault()))
            }.getOrDefault("--")
        }
    }
}
