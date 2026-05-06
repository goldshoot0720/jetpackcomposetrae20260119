package com.example.jetpackcomposetrae20260119.data

data class FengTubeChannel(
    val sourceUrl: String,
    val fallbackName: String
)

data class FengTubeVideo(
    val title: String,
    val url: String,
    val channelTitle: String,
    val publishedAt: String
)

data class FengTubeChannelFeed(
    val channel: FengTubeChannel,
    val channelTitle: String,
    val videos: List<FengTubeVideo>,
    val fetchedAt: String,
    val errorMessage: String? = null
)
