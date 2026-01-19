package com.example.jetpackcomposetrae20260119.data

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Databases
import io.appwrite.Query
import io.appwrite.ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AppwriteRepository(context: Context) {
    private val client = Client(context)
        .setEndpoint(Constants.ENDPOINT)
        .setProject(Constants.PROJECT_ID)
        .setSelfSigned(true)

    private val databases = Databases(client)

    suspend fun getSubscriptions(): List<Subscription> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.SUBSCRIPTION_COLLECTION_ID,
                queries = listOf(
                    Query.orderAsc("nextdate")
                )
            )
            response.documents.map { Subscription.fromDocument(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addSubscription(subscription: Subscription) = withContext(Dispatchers.IO) {
        try {
            databases.createDocument(
                databaseId = Constants.DATABASE_ID,
                collectionId = Constants.SUBSCRIPTION_COLLECTION_ID,
                documentId = ID.unique(),
                data = Subscription.toMap(subscription)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getUpcomingSubscriptions(days: Int = 3): List<Subscription> = withContext(Dispatchers.IO) {
        try {
            // Fetching all for simplicity as we need to parse dates
            val all = getSubscriptions()
            val today = LocalDate.now()
            val limitDate = today.plusDays(days.toLong())

            all.filter {
                try {
                    if (it.nextDate.isBlank()) return@filter false
                    // Appwrite returns ISO string, e.g., 2023-01-01T00:00:00.000+00:00
                    val itemDate = ZonedDateTime.parse(it.nextDate).toLocalDate()
                    // Include today and up to limitDate
                    !itemDate.isBefore(today) && !itemDate.isAfter(limitDate)
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
