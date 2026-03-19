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
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class OilPriceRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun refreshLatestPrice(): OilPricePoint? = withContext(Dispatchers.IO) {
        runCatching {
            val html = fetchHomepageHtml()
            val latestPoint = parseLatestPoint(html)
            if (latestPoint != null) {
                savePoint(latestPoint)
            }
            latestPoint
        }.onFailure { error ->
            Log.e(TAG, "Failed to refresh oil price", error)
        }.getOrNull()
    }

    fun getSavedHistory(): List<OilPricePoint> {
        val raw = prefs.getString(HISTORY_KEY, null).orEmpty()
        if (raw.isBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(
                        OilPricePoint(
                            tradeDate = item.optString("tradeDate"),
                            displayDate = item.optString("displayDate"),
                            price = item.optDouble("price"),
                            fetchedAt = item.optString("fetchedAt")
                        )
                    )
                }
            }.sortedBy { it.tradeDate }
        }.getOrElse {
            Log.e(TAG, "Failed to read oil price history", it)
            emptyList()
        }
    }

    private fun fetchHomepageHtml(): String {
        val connection = URL(Constants.OIL_PRICE_URL).openConnection() as HttpURLConnection
        return connection.run {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Mobile Safari/537.36"
            )
            inputStream.bufferedReader().use { it.readText() }
        }
    }

    private fun parseLatestPoint(html: String): OilPricePoint? {
        val normalized = html
            .replace("&nbsp;", " ")
            .replace(Regex("<[^>]+>"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val sentenceMatch = Regex(
            pattern = """OQD(?:\s+Daily)?\s+Marker Price\s+([A-Za-z]+\s+\d{1,2},\s+\d{4})\s+is\s+([0-9]+(?:\.[0-9]+)?)""",
            options = setOf(RegexOption.IGNORE_CASE)
        ).find(normalized)

        val summaryMatch = Regex(
            pattern = """OQD(?:\s+Daily)?\s+Marker Price\s+([0-9]+(?:\.[0-9]+)?)\s+(\d{1,2}\s+[A-Za-z]{3}(?:[-,]\s*\d{4}))""",
            options = setOf(RegexOption.IGNORE_CASE)
        ).find(normalized)

        val displayDate: String
        val price: Double

        when {
            sentenceMatch != null -> {
                displayDate = sentenceMatch.groupValues[1].trim()
                price = sentenceMatch.groupValues[2].toDoubleOrNull() ?: return null
            }
            summaryMatch != null -> {
                price = summaryMatch.groupValues[1].toDoubleOrNull() ?: return null
                displayDate = summaryMatch.groupValues[2].trim()
            }
            else -> {
                Log.w(TAG, "Unable to parse OQD price from homepage content")
                return null
            }
        }

        val tradeDate = parseTradeDate(displayDate) ?: return null

        return OilPricePoint(
            tradeDate = tradeDate.toString(),
            displayDate = displayDate,
            price = price,
            fetchedAt = Instant.now().toString()
        )
    }

    private fun parseTradeDate(displayDate: String): LocalDate? {
        val normalizedDate = displayDate.replace(Regex("\\s+"), " ").trim()
        val patterns = listOf(
            "MMMM d, yyyy",
            "MMM d, yyyy",
            "d MMM-yyyy",
            "dd MMM-yyyy",
            "d MMM, yyyy",
            "dd MMM, yyyy"
        )

        for (pattern in patterns) {
            try {
                return LocalDate.parse(
                    normalizedDate,
                    DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                )
            } catch (_: DateTimeParseException) {
                // Try the next supported date shape from the website.
            }
        }

        Log.w(TAG, "Unable to parse trade date: $displayDate")
        return null
    }

    private fun savePoint(point: OilPricePoint) {
        val merged = (getSavedHistory() + point)
            .associateBy { it.tradeDate }
            .values
            .sortedBy { it.tradeDate }
            .takeLast(MAX_HISTORY_SIZE)

        val array = JSONArray()
        merged.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("tradeDate", item.tradeDate)
                    put("displayDate", item.displayDate)
                    put("price", item.price)
                    put("fetchedAt", item.fetchedAt)
                }
            )
        }

        prefs.edit().putString(HISTORY_KEY, array.toString()).apply()
    }

    companion object {
        private const val TAG = "OilPriceRepository"
        private const val PREFS_NAME = "oil_price_monitor"
        private const val HISTORY_KEY = "oqd_price_history"
        private const val MAX_HISTORY_SIZE = 90

        fun formatFetchedAt(isoInstant: String): String {
            return runCatching {
                val instant = Instant.parse(isoInstant)
                val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                formatter.format(instant.atZone(ZoneId.systemDefault()))
            }.getOrDefault("--")
        }
    }
}
