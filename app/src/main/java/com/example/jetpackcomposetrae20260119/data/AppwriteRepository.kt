package com.example.jetpackcomposetrae20260119.data

import android.content.Context
import android.util.Log
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.services.Databases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.time.ZonedDateTime

class AppwriteRepository(context: Context) {
    private val client = Client(context)
        .setEndpoint(Constants.ENDPOINT)
        .setProject(Constants.PROJECT_ID)
        .setSelfSigned(true)

    private val databases = Databases(client)
    private val resolvedTableIds = mutableMapOf<String, String>()

    suspend fun getSubscriptions(): List<Subscription> = withContext(Dispatchers.IO) {
        getSubscriptionsFromDynamicTable().ifEmpty {
            getSubscriptionsFromLegacyCollection()
        }
    }

    suspend fun addSubscription(subscription: Subscription) = withContext(Dispatchers.IO) {
        if (addSubscriptionToDynamicTable(subscription)) {
            return@withContext
        }

        addSubscriptionToLegacyCollection(subscription)
    }

    suspend fun getUpcomingSubscriptions(days: Int = 3): List<Subscription> = withContext(Dispatchers.IO) {
        try {
            val all = getSubscriptions()
            val today = LocalDate.now()
            val limitDate = today.plusDays(days.toLong())

            all.filter {
                try {
                    if (it.nextDate.isBlank()) return@filter false
                    val itemDate = ZonedDateTime.parse(it.nextDate).toLocalDate()
                    !itemDate.isBefore(today) && !itemDate.isAfter(limitDate)
                } catch (_: Exception) {
                    false
                }
            }
        } catch (error: Exception) {
            Log.e(TAG, "Error filtering upcoming subscriptions", error)
            emptyList()
        }
    }

    suspend fun resolveFoodTableId(): String? = withContext(Dispatchers.IO) {
        resolveAccessibleTableId(
            cacheKey = FOOD_TABLE_CACHE_KEY,
            tableNames = listOf(
                Constants.FOOD_COLLECTION_NAME,
                "foods"
            ),
            fallbackIds = listOf(
                Constants.FOOD_COLLECTION_NAME,
                "foods_table"
            )
        )
    }

    suspend fun ping() = withContext(Dispatchers.IO) {
        try {
            client.ping()
            resolveFoodTableId()
            Log.d(TAG, "Ping successful")
        } catch (error: Exception) {
            Log.e(TAG, "Ping failed", error)
        }
    }

    private suspend fun getSubscriptionsFromDynamicTable(): List<Subscription> {
        return try {
            val tableId = resolveSubscriptionTableId() ?: return emptyList()
            val allSubscriptions = mutableListOf<Subscription>()
            var offset = 0
            val limit = 100

            while (true) {
                val rows = listRowsFromTable(
                    tableId = tableId,
                    queries = listOf(
                        Query.orderAsc("nextdate"),
                        Query.limit(limit),
                        Query.offset(offset)
                    )
                )

                if (rows.isEmpty()) {
                    break
                }

                allSubscriptions += rows
                if (rows.size < limit) {
                    break
                }
                offset += limit
            }

            Log.d(TAG, "Fetched ${allSubscriptions.size} subscriptions from table $tableId")
            allSubscriptions
        } catch (error: Exception) {
            Log.e(TAG, "Dynamic subscription table lookup failed", error)
            emptyList()
        }
    }

    private suspend fun addSubscriptionToDynamicTable(subscription: Subscription): Boolean {
        return try {
            val tableId = resolveSubscriptionTableId() ?: return false
            createRow(
                tableId = tableId,
                data = Subscription.toMap(subscription)
            )
            Log.d(TAG, "Subscription added through dynamic table $tableId")
            true
        } catch (error: Exception) {
            Log.e(TAG, "Dynamic subscription table insert failed", error)
            false
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun getSubscriptionsFromLegacyCollection(): List<Subscription> {
        return try {
            Log.d(TAG, "Falling back to legacy collection API for subscriptions")
            val allSubscriptions = mutableListOf<Subscription>()
            var offset = 0
            val limit = 100

            while (true) {
                val response = databases.listDocuments(
                    databaseId = Constants.DATABASE_ID,
                    collectionId = Constants.SUBSCRIPTION_COLLECTION_ID,
                    queries = listOf(
                        Query.orderAsc("nextdate"),
                        Query.limit(limit),
                        Query.offset(offset)
                    )
                )

                val batch = response.documents.map(Subscription::fromDocument)
                allSubscriptions += batch

                if (batch.size < limit) {
                    break
                }
                offset += limit
            }

            allSubscriptions
        } catch (error: Exception) {
            Log.e(TAG, "Legacy subscription fetch failed", error)
            emptyList()
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun addSubscriptionToLegacyCollection(subscription: Subscription) {
        Log.d(TAG, "Falling back to legacy collection insert for subscriptions")
        databases.createDocument(
            databaseId = Constants.DATABASE_ID,
            collectionId = Constants.SUBSCRIPTION_COLLECTION_ID,
            documentId = ID.unique(),
            data = Subscription.toMap(subscription)
        )
    }

    private fun resolveSubscriptionTableId(): String? {
        return resolveAccessibleTableId(
            cacheKey = SUBSCRIPTION_TABLE_CACHE_KEY,
            tableNames = listOf(
                Constants.SUBSCRIPTION_COLLECTION_NAME,
                "subscriptions"
            ),
            fallbackIds = listOf(
                Constants.SUBSCRIPTION_COLLECTION_ID
            )
        )
    }

    private fun resolveAccessibleTableId(
        cacheKey: String,
        tableNames: List<String>,
        fallbackIds: List<String>
    ): String? {
        resolvedTableIds[cacheKey]?.let { return it }

        val dynamicCandidates = findDynamicTableIds(tableNames)
        val orderedCandidates = (dynamicCandidates + fallbackIds).distinct()

        orderedCandidates.forEach { candidate ->
            try {
                listRowsFromTable(
                    tableId = candidate,
                    queries = listOf(Query.limit(1))
                )
                resolvedTableIds[cacheKey] = candidate
                Log.d(TAG, "Resolved table $cacheKey -> $candidate")
                return candidate
            } catch (error: Exception) {
                Log.d(TAG, "Table candidate $candidate is not available yet", error)
            }
        }

        return null
    }

    private fun findDynamicTableIds(tableNames: List<String>): List<String> {
        return try {
            val response = executeJsonRequest(
                url = "${Constants.ENDPOINT}/tablesdb/${Constants.DATABASE_ID}/tables",
                method = "GET"
            )
            val tables = response.optJSONArray("tables") ?: JSONArray()
            buildList {
                for (index in 0 until tables.length()) {
                    val table = tables.optJSONObject(index) ?: continue
                    val name = table.optString("name")
                    if (tableNames.none { it.equals(name, ignoreCase = true) }) {
                        continue
                    }

                    val tableId = table.optString("\$id")
                        .ifBlank { table.optString("tableId") }
                        .ifBlank { table.optString("id") }

                    if (tableId.isNotBlank()) {
                        add(tableId)
                    }
                }
            }
        } catch (error: Exception) {
            Log.d(TAG, "Dynamic table listing unavailable, will fall back to fixed ids", error)
            emptyList()
        }
    }

    private fun listRowsFromTable(
        tableId: String,
        queries: List<String>
    ): List<Subscription> {
        val queryString = buildString {
            if (queries.isNotEmpty()) {
                append("?")
                append(
                    queries.joinToString("&") { query ->
                        "queries[]=${urlEncode(query)}"
                    }
                )
            }
        }

        val url = "${Constants.ENDPOINT}/tablesdb/${Constants.DATABASE_ID}/tables/$tableId/rows$queryString"
        val response = executeJsonRequest(
            url = url,
            method = "GET"
        )

        val rows = response.optJSONArray("rows") ?: JSONArray()
        return buildList {
            for (index in 0 until rows.length()) {
                val row = rows.optJSONObject(index) ?: continue
                add(
                    Subscription.fromRow(
                        rowId = row.optString("\$id"),
                        data = row.toMap(),
                        createdAt = row.optString("\$createdAt"),
                        updatedAt = row.optString("\$updatedAt")
                    )
                )
            }
        }
    }

    private fun createRow(
        tableId: String,
        data: Map<String, Any>
    ) {
        val payload = JSONObject().apply {
            put("rowId", ID.unique())
            put("data", JSONObject(data))
        }

        executeJsonRequest(
            url = "${Constants.ENDPOINT}/tablesdb/${Constants.DATABASE_ID}/tables/$tableId/rows",
            method = "POST",
            requestBody = payload.toString()
        )
    }

    private fun executeJsonRequest(
        url: String,
        method: String,
        requestBody: String? = null
    ): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        return connection.run {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 15_000
            doInput = true
            setRequestProperty("X-Appwrite-Project", Constants.PROJECT_ID)
            setRequestProperty("X-Appwrite-Response-Format", "1.8.0")
            setRequestProperty("Accept", "application/json")
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Mobile Safari/537.36"
            )

            if (requestBody != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(requestBody)
                }
            }

            val responseText = try {
                val stream = if (responseCode in 200..299) inputStream else errorStream
                stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            } finally {
                disconnect()
            }

            if (responseCode !in 200..299) {
                throw IllegalStateException("Appwrite request failed ($responseCode): $responseText")
            }

            JSONObject(responseText)
        }
    }

    private fun JSONObject.toMap(): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        keys().forEach { key ->
            when (val value = opt(key)) {
                JSONObject.NULL -> Unit
                is JSONObject -> result[key] = value.toMap()
                is JSONArray -> {
                    val items = mutableListOf<Any>()
                    for (index in 0 until value.length()) {
                        val item = value.opt(index) ?: continue
                        if (item != JSONObject.NULL) {
                            items += item
                        }
                    }
                    result[key] = items
                }
                else -> if (value != null) {
                    result[key] = value
                }
            }
        }
        return result
    }

    private fun urlEncode(value: String): String {
        return URLEncoder.encode(value, Charsets.UTF_8.name())
    }

    companion object {
        private const val TAG = "AppwriteRepository"
        private const val SUBSCRIPTION_TABLE_CACHE_KEY = "subscription_table"
        private const val FOOD_TABLE_CACHE_KEY = "food_table"
    }
}
