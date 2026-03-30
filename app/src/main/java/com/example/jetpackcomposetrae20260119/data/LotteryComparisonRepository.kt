package com.example.jetpackcomposetrae20260119.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class LotteryComparisonRepository {
    suspend fun fetchDashboard(): LotteryDashboard = withContext(Dispatchers.IO) {
        val endMonth = YearMonth.now()
        val startMonth = endMonth.minusMonths(2)
        val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

        val startMonthText = startMonth.format(monthFormatter)
        val endMonthText = endMonth.format(monthFormatter)
        val query = "month=$startMonthText&endMonth=$endMonthText&pageNum=1&pageSize=200"

        coroutineScope {
            val superLottoDeferred = async {
                fetchSection(
                    endpoint = "SuperLotto638Result",
                    sectionId = "super_lotto638",
                    title = "\u5a01\u529b\u5f69",
                    sourceUrl = "https://www.taiwanlottery.com/lotto/result/super_lotto638",
                    arrayKey = "superLotto638Res",
                    query = query,
                    tickets = SUPER_LOTTO_TICKETS
                )
            }
            val lotto649Deferred = async {
                fetchSection(
                    endpoint = "Lotto649Result",
                    sectionId = "lotto649",
                    title = "\u5927\u6a02\u900f",
                    sourceUrl = "https://www.taiwanlottery.com/lotto/result/lotto649",
                    arrayKey = "lotto649Res",
                    query = query,
                    tickets = LOTTO649_TICKETS
                )
            }
            val daily539Deferred = async {
                fetchSection(
                    endpoint = "Daily539Result",
                    sectionId = "daily539",
                    title = "\u4eca\u5f69539",
                    sourceUrl = "https://www.taiwanlottery.com/lotto/result/daily_cash",
                    arrayKey = "daily539Res",
                    query = query,
                    tickets = DAILY539_TICKETS
                )
            }

            val sections = awaitAll(superLottoDeferred, lotto649Deferred, daily539Deferred)
            LotteryDashboard(
                rangeStartMonth = startMonthText,
                rangeEndMonth = endMonthText,
                sections = sections
            )
        }
    }

    private fun fetchSection(
        endpoint: String,
        sectionId: String,
        title: String,
        sourceUrl: String,
        arrayKey: String,
        query: String,
        tickets: List<LotteryTicket>
    ): LotterySection {
        val url = "${Constants.TAIWAN_LOTTERY_API_BASE}/Lottery/$endpoint?$query"
        val response = fetchJson(url)
        val content = response.getJSONObject("content")
        val items = content.getJSONArray(arrayKey)

        return LotterySection(
            id = sectionId,
            title = title,
            sourceUrl = sourceUrl,
            draws = parseDraws(items),
            tickets = tickets
        )
    }

    private fun fetchJson(url: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        return connection.run {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Mobile Safari/537.36"
            )
            setRequestProperty("Accept", "application/json")

            try {
                val body = inputStream.bufferedReader().use { it.readText() }
                JSONObject(body)
            } finally {
                disconnect()
            }
        }
    }

    private fun parseDraws(items: JSONArray): List<LotteryDraw> {
        return buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                val drawNumbers = item.optJSONArray("drawNumberSize") ?: continue
                if (drawNumbers.length() < 5) continue

                val allNumbers = drawNumbers.toIntList()
                val hasSpecial = allNumbers.size > 5

                add(
                    LotteryDraw(
                        period = item.optLong("period").toString(),
                        lotteryDate = formatDate(item.optString("lotteryDate")),
                        numbers = if (hasSpecial) allNumbers.dropLast(1) else allNumbers,
                        specialNumber = if (hasSpecial) allNumbers.last() else null
                    )
                )
            }
        }
    }

    private fun formatDate(rawDate: String): String {
        return runCatching {
            LocalDate.parse(rawDate.take(10)).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        }.getOrElse {
            Log.w(TAG, "Failed to parse lottery date: $rawDate", it)
            rawDate
        }
    }

    private fun JSONArray.toIntList(): List<Int> {
        return buildList {
            for (index in 0 until length()) {
                add(optInt(index))
            }
        }
    }

    companion object {
        private const val TAG = "LotteryComparisonRepo"

        private val SUPER_LOTTO_TICKETS = listOf(
            LotteryTicket("\u7b2c\u4e00\u7d44", listOf(7, 11, 23, 32, 33, 38), 2),
            LotteryTicket("\u7b2c\u4e8c\u7d44", listOf(7, 11, 23, 32, 33, 38), 1),
            LotteryTicket("\u7b2c\u4e09\u7d44", listOf(19, 8, 11, 27, 37, 16), 8),
            LotteryTicket("\u7b2c\u56db\u7d44", listOf(19, 8, 4, 3, 37, 16), 8)
        )

        private val LOTTO649_TICKETS = listOf(
            LotteryTicket("\u7b2c\u4e00\u7d44", listOf(19, 8, 11, 27, 37, 16)),
            LotteryTicket("\u7b2c\u4e8c\u7d44", listOf(19, 8, 4, 3, 37, 16))
        )

        private val DAILY539_TICKETS = listOf(
            LotteryTicket("\u7b2c\u4e00\u7d44", listOf(19, 8, 11, 27, 37)),
            LotteryTicket("\u7b2c\u4e8c\u7d44", listOf(19, 8, 4, 3, 37))
        )
    }
}
