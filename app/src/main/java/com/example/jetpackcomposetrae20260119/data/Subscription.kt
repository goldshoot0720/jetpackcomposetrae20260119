package com.example.jetpackcomposetrae20260119.data

import io.appwrite.models.Document
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Subscription(
    val id: String,
    val name: String,
    val site: String,
    val price: Int,
    val nextDate: String, // ISO String
    val note: String,
    val account: String,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun fromDocument(document: Document<Map<String, Any>>): Subscription {
            val data = document.data
            return Subscription(
                id = document.id,
                name = data["name"] as? String ?: "",
                site = data["site"] as? String ?: "",
                price = (data["price"] as? Number)?.toInt() ?: 0,
                nextDate = data["nextdate"] as? String ?: "",
                note = data["note"] as? String ?: "",
                account = data["account"] as? String ?: "",
                createdAt = document.createdAt,
                updatedAt = document.updatedAt
            )
        }
        
        fun toMap(subscription: Subscription): Map<String, Any> {
             return mapOf(
                "name" to subscription.name,
                "site" to subscription.site,
                "price" to subscription.price,
                "nextdate" to subscription.nextDate,
                "note" to subscription.note,
                "account" to subscription.account
            )
        }
    }
}
