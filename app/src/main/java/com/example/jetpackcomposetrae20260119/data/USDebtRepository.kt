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
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0 Mobile Safari/537.36"
            )
            setRequestProperty("Accept-Language", "en-US,en;q=0.9")
            inputStream.bufferedReader().use { it.readText() }
        }
    }

    private fun parseNationalDebt(html: String): USDebtPoint? {
        val now = Instant.now()
        val preferredElementId = findLayer29ElementId(html)
        val candidates = parseScriptCandidates(html, now)
        val bestCandidate = preferredElementId?.let { preferredId ->
            candidates.firstOrNull { it.elementId == preferredId }
        } ?: candidates.maxByOrNull { it.debt }

        if (bestCandidate == null) {
            Log.w(TAG, "Unable to parse US national debt from current homepage")
            return null
        }

        return USDebtPoint(
            capturedAt = now.toString(),
            debt = bestCandidate.debt
        )
    }

    private fun findLayer29ElementId(html: String): String? {
        val directMatch = Regex(
            """<div[^>]*id\s*=\s*["']layer29["'][^>]*>.*?<span[^>]*id\s*=\s*["']([^"']+)["']""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ).find(html)

        if (directMatch != null) {
            return directMatch.groupValues[1]
        }

        return Regex("""#(X[A-Za-z0-9]+)\s*\{[^}]*font-size:\s*18pt""")
            .find(html)
            ?.groupValues
            ?.getOrNull(1)
    }

    private fun parseScriptCandidates(html: String, now: Instant): List<DebtCandidate> {
        val markerRegex = Regex(
            """document\.getElementById\s*\(\s*['"]([A-Za-z0-9]+)['"]\s*\)\s*\.firstChild\.nodeValue\s*=\s*Assign"""
        )

        return markerRegex.findAll(html).mapNotNull { match ->
            val elementId = match.groupValues[1]
            val snippetStart = (match.range.first - 1800).coerceAtLeast(0)
            val snippetEnd = (match.range.last + 200).coerceAtMost(html.lastIndex)
            val snippet = html.substring(snippetStart, snippetEnd + 1)
            parseCandidateFromSnippet(elementId, snippet, now)
        }.toList()
    }

    private fun parseCandidateFromSnippet(
        elementId: String,
        snippet: String,
        now: Instant
    ): DebtCandidate? {
        val normalizedSnippet = snippet
            .replace(Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL), " ")
            .replace(Regex("""\s+"""), " ")

        val formulaMatch = Regex(
            pattern = """
                var\s+$elementId\s*=\s*([0-9]+(?:\.[0-9]+)?)\s*;\s*
                var\s+([A-Za-z0-9_]+)\s*=\s*([0-9]+(?:\.[0-9]+)?)\s*;\s*
                var\s+([A-Za-z0-9_]+)\s*=\s*([0-9]+(?:\.[0-9]+)?)\s*;\s*
                var\s+Class\s*=\s*new\s+Date\(\)\s*;\s*
                var\s+Method\s*=\s*Class\.getTime\(\)\s*/\s*1000\s*-\s*\4\s*;\s*
                var\s+Public\s*=\s*$elementId\s*\+\s*Method\s*\*\s*\2
            """.trimIndent(),
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.COMMENTS)
        ).find(normalizedSnippet)

        val baseValue = formulaMatch?.groupValues?.getOrNull(1)?.toDoubleOrNull()
            ?: return null
        val ratePerSecond = formulaMatch.groupValues.getOrNull(3)?.toDoubleOrNull()
            ?: return null
        val anchorValue = formulaMatch.groupValues.getOrNull(5)?.toDoubleOrNull()
            ?: return null

        val anchorSeconds = when {
            anchorValue < 1_000_000 -> anchorValue * 86_400.0
            else -> anchorValue
        }

        val debt = baseValue + (now.epochSecond - anchorSeconds) * ratePerSecond
        if (!debt.isFinite() || debt <= 0 || debt < 100_000_000_000.0 || debt > 1_000_000_000_000_000.0) {
            return null
        }

        return DebtCandidate(
            elementId = elementId,
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

    private data class DebtCandidate(
        val elementId: String,
        val debt: Double
    )

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
