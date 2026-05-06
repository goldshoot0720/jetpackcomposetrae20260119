package com.example.jetpackcomposetrae20260119.data

data class FengFinanceInstrument(
    val name: String,
    val symbol: String,
    val sourceUrl: String
)

data class FengFinanceQuote(
    val instrument: FengFinanceInstrument,
    val priceLabel: String,
    val numericPrice: Double?,
    val changeLabel: String,
    val fetchedAt: String,
    val isNewHigh: Boolean,
    val isNewLow: Boolean,
    val errorMessage: String? = null
)
