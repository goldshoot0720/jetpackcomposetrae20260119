package com.example.jetpackcomposetrae20260119.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

class FengFinanceRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun refreshQuotes(): List<FengFinanceQuote> = withContext(Dispatchers.IO) {
        val quotes = INSTRUMENTS.map { instrument ->
            runCatching {
                fetchQuote(instrument)
            }.getOrElse { error ->
                Log.e(TAG, "Failed to load quote: ${instrument.symbol}", error)
                getCachedQuotes()
                    .firstOrNull { it.instrument.symbol == instrument.symbol }
                    ?.copy(errorMessage = error.message ?: "讀取失敗")
                    ?: FengFinanceQuote(
                        instrument = instrument,
                        priceLabel = "--",
                        numericPrice = null,
                        changeLabel = "--",
                        fetchedAt = Instant.now().toString(),
                        isNewHigh = false,
                        isNewLow = false,
                        errorMessage = error.message ?: "讀取失敗"
                    )
            }
        }
        saveQuotes(quotes)
        quotes
    }

    fun getCachedQuotes(): List<FengFinanceQuote> {
        val raw = prefs.getString(QUOTES_KEY, null).orEmpty()
        if (raw.isBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val instrument = FengFinanceInstrument(
                        name = item.optString("name"),
                        symbol = item.optString("symbol"),
                        sourceUrl = item.optString("sourceUrl")
                    )
                    add(
                        FengFinanceQuote(
                            instrument = instrument,
                            priceLabel = item.optString("priceLabel", "--"),
                            numericPrice = item.optDoubleOrNull("numericPrice"),
                            changeLabel = item.optString("changeLabel", "--"),
                            fetchedAt = item.optString("fetchedAt"),
                            isNewHigh = item.optBoolean("isNewHigh", false),
                            isNewLow = item.optBoolean("isNewLow", false),
                            errorMessage = item.optString("errorMessage").ifBlank { null }
                        )
                    )
                }
            }
        }.getOrElse {
            Log.e(TAG, "Failed to read cached Feng finance quotes", it)
            emptyList()
        }
    }

    private fun fetchQuote(instrument: FengFinanceInstrument): FengFinanceQuote {
        val html = fetchText(instrument.sourceUrl)
        val quoteText = yahooChartSymbol(instrument.symbol)
            ?.let { chartSymbol ->
                "$html\n${runCatching { fetchText("https://query1.finance.yahoo.com/v8/finance/chart/$chartSymbol?region=TW&lang=zh-TW") }.getOrDefault("")}"
            }
            ?: html
        val price = parsePrice(quoteText)
        val change = parseChange(quoteText)
        val flags = updateHighLowFlags(instrument.symbol, price)
        val isHistoricShillerHigh = instrument.symbol == SHILLER_PE_SYMBOL &&
            price != null &&
            price > SHILLER_PE_HISTORIC_MAX

        return FengFinanceQuote(
            instrument = instrument,
            priceLabel = price?.let(::formatPrice) ?: "--",
            numericPrice = price,
            changeLabel = change ?: "--",
            fetchedAt = Instant.now().toString(),
            isNewHigh = flags.first || isHistoricShillerHigh,
            isNewLow = flags.second
        )
    }

    private fun fetchText(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        return connection.run {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Mobile Safari/537.36"
            )
            setRequestProperty("Accept-Language", "zh-TW,zh;q=0.9,en;q=0.8")
            setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            val stream = if (responseCode in 200..299) inputStream else errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            disconnect()
            if (responseCode !in 200..299) {
                error("HTTP $responseCode")
            }
            text
        }
    }

    private fun parsePrice(html: String): Double? {
        val patterns = listOf(
            Regex(""""last"\s*:\s*"?(-?\d[\d,]*(?:\.\d+)?)"?"""),
            Regex(""""lastPrice"\s*:\s*"?(-?\d[\d,]*(?:\.\d+)?)"?"""),
            Regex(""""price"\s*:\s*"?(-?\d[\d,]*(?:\.\d+)?)"?"""),
            Regex(""""regularMarketPrice"\s*:\s*"?(-?\d[\d,]*(?:\.\d+)?)"?"""),
            Regex(""""previousClose"\s*:\s*"?(-?\d[\d,]*(?:\.\d+)?)"?"""),
            Regex("""Current Shiller PE Ratio:\s*(-?\d[\d,]*(?:\.\d+)?)""", RegexOption.IGNORE_CASE),
            Regex("""QuoteStrip-lastPrice[^>]*>\s*([^<]+)"""),
            Regex("""Latest Price[^0-9-]*(-?\d[\d,]*(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
        )

        return patterns.firstNotNullOfOrNull { pattern ->
            pattern.find(html)
                ?.groupValues
                ?.getOrNull(1)
                ?.stripHtml()
                ?.toMarketDouble()
        }
    }

    private fun parseChange(html: String): String? {
        val value = listOf(
            Regex(""""change"\s*:\s*"?(-?\d[\d,]*(?:\.\d+)?)"?"""),
            Regex(""""priceChange"\s*:\s*"?(-?\d[\d,]*(?:\.\d+)?)"?"""),
            Regex(""""regularMarketChange"\s*:\s*"?(-?\d[\d,]*(?:\.\d+)?)"?"""),
            Regex("""Current Shiller PE Ratio:\s*-?\d[\d,]*(?:\.\d+)?\s+([+-]\d[\d,]*(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
        ).firstNotNullOfOrNull { pattern ->
            pattern.find(html)?.groupValues?.getOrNull(1)?.stripHtml()?.toMarketDouble()
        }

        val percent = listOf(
            Regex(""""change_pct"\s*:\s*"?(-?\d[\d,]*(?:\.\d+)?)"?"""),
            Regex(""""changePercent"\s*:\s*"?(-?\d[\d,]*(?:\.\d+)?)"?"""),
            Regex(""""percentChange"\s*:\s*"?(-?\d[\d,]*(?:\.\d+)?)"?"""),
            Regex(""""regularMarketChangePercent"\s*:\s*"?(-?\d[\d,]*(?:\.\d+)?)"?"""),
            Regex("""Current Shiller PE Ratio:\s*-?\d[\d,]*(?:\.\d+)?\s+[+-]\d[\d,]*(?:\.\d+)?\s*\(([+-]?\d[\d,]*(?:\.\d+)?)%?\)""", RegexOption.IGNORE_CASE)
        ).firstNotNullOfOrNull { pattern ->
            pattern.find(html)?.groupValues?.getOrNull(1)?.stripHtml()?.toMarketDouble()
        }

        return when {
            value != null && percent != null -> "${signed(value)} (${signed(percent)}%)"
            value != null -> signed(value)
            percent != null -> "${signed(percent)}%"
            else -> null
        }
    }

    private fun updateHighLowFlags(symbol: String, price: Double?): Pair<Boolean, Boolean> {
        if (price == null) return false to false

        val highKey = "$symbol.high"
        val lowKey = "$symbol.low"
        val oldHigh = prefs.getString(highKey, null)?.toDoubleOrNull()
        val oldLow = prefs.getString(lowKey, null)?.toDoubleOrNull()

        val isNewHigh = oldHigh != null && price > oldHigh
        val isNewLow = oldLow != null && price < oldLow
        val nextHigh = maxOf(oldHigh ?: price, price)
        val nextLow = minOf(oldLow ?: price, price)

        prefs.edit()
            .putString(highKey, nextHigh.toString())
            .putString(lowKey, nextLow.toString())
            .apply()

        return isNewHigh to isNewLow
    }

    private fun saveQuotes(quotes: List<FengFinanceQuote>) {
        val array = JSONArray()
        quotes.forEach { quote ->
            array.put(
                JSONObject().apply {
                    put("name", quote.instrument.name)
                    put("symbol", quote.instrument.symbol)
                    put("sourceUrl", quote.instrument.sourceUrl)
                    put("priceLabel", quote.priceLabel)
                    put("numericPrice", quote.numericPrice ?: JSONObject.NULL)
                    put("changeLabel", quote.changeLabel)
                    put("fetchedAt", quote.fetchedAt)
                    put("isNewHigh", quote.isNewHigh)
                    put("isNewLow", quote.isNewLow)
                    put("errorMessage", quote.errorMessage.orEmpty())
                }
            )
        }
        prefs.edit().putString(QUOTES_KEY, array.toString()).apply()
    }

    private fun JSONObject.optDoubleOrNull(name: String): Double? {
        return if (isNull(name)) null else optDouble(name).takeUnless { it.isNaN() }
    }

    private fun String.stripHtml(): String {
        return replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .trim()
    }

    private fun String.toMarketDouble(): Double? {
        return replace(",", "")
            .trim()
            .takeIf { it.isNotBlank() && it != "--" }
            ?.toDoubleOrNull()
    }

    private fun formatPrice(value: Double): String {
        val pattern = if (abs(value) >= 1_000) "#,##0.##" else "#,##0.####"
        return DecimalFormat(pattern).format(value)
    }

    private fun signed(value: Double): String {
        val label = formatPrice(abs(value))
        return if (value > 0) "+$label" else if (value < 0) "-$label" else label
    }

    private fun yahooChartSymbol(symbol: String): String? {
        return when (symbol) {
            "^TWII" -> "%5ETWII"
            "2330.TW" -> "2330.TW"
            else -> null
        }
    }

    companion object {
        private const val TAG = "FengFinanceRepository"
        private const val PREFS_NAME = "feng_finance"
        private const val QUOTES_KEY = "quotes"
        const val SHILLER_PE_SYMBOL = "SHILLER_PE"
        const val SHILLER_PE_HISTORIC_MAX = 44.19

        val INSTRUMENTS = listOf(
            FengFinanceInstrument("加權指數", "^TWII", "https://tw.stock.yahoo.com/s/tse.php"),
            FengFinanceInstrument("台積電", "2330.TW", "https://tw.stock.yahoo.com/quote/2330.TW"),
            FengFinanceInstrument("Shiller PE Ratio", SHILLER_PE_SYMBOL, "https://www.multpl.com/shiller-pe"),
            FengFinanceInstrument("Nikkei 225 Index", ".N225", "https://www.cnbc.com/quotes/.N225"),
            FengFinanceInstrument("KOSPI Index", ".KS11", "https://www.cnbc.com/quotes/.KS11?qsearchterm=kospi"),
            FengFinanceInstrument("ICE Brent Crude", "@LCO.1", "https://www.cnbc.com/quotes/@LCO.1"),
            FengFinanceInstrument("U.S. 30 Year Treasury", "US30Y", "https://www.cnbc.com/quotes/US30Y"),
            FengFinanceInstrument("Gold COMEX", "@GC.1", "https://www.cnbc.com/quotes/@GC.1"),
            FengFinanceInstrument("Dow Jones Industrial Average", ".DJI", "https://www.cnbc.com/quotes/.DJI"),
            FengFinanceInstrument("S&P 500 Index", ".SPX", "https://www.cnbc.com/quotes/.SPX"),
            FengFinanceInstrument("NASDAQ Composite", ".IXIC", "https://www.cnbc.com/quotes/.IXIC"),
            FengFinanceInstrument("CBOE Volatility Index", ".VIX", "https://www.cnbc.com/quotes/.VIX"),
            FengFinanceInstrument("Bitcoin/USD Coin Metrics", "BTC.CM=", "https://www.cnbc.com/quotes/BTC.CM="),
            FengFinanceInstrument("Ether/USD Coin Metrics", "ETH.CM=", "https://www.cnbc.com/quotes/ETH.CM=")
        )

        fun formatFetchedAt(isoInstant: String): String {
            return runCatching {
                val instant = Instant.parse(isoInstant)
                val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                formatter.format(instant.atZone(ZoneId.systemDefault()))
            }.getOrDefault("--")
        }
    }
}
