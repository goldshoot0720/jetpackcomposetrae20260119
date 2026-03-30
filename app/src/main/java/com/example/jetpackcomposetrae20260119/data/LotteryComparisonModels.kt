package com.example.jetpackcomposetrae20260119.data

data class LotteryDashboard(
    val rangeStartMonth: String,
    val rangeEndMonth: String,
    val sections: List<LotterySection>
)

data class LotterySection(
    val id: String,
    val title: String,
    val sourceUrl: String,
    val draws: List<LotteryDraw>,
    val tickets: List<LotteryTicket>
)

data class LotteryDraw(
    val period: String,
    val lotteryDate: String,
    val numbers: List<Int>,
    val specialNumber: Int? = null
)

data class LotteryTicket(
    val label: String,
    val numbers: List<Int>,
    val specialNumber: Int? = null
)
