package com.example.jetpackcomposetrae20260119.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcomposetrae20260119.data.FengTubeChannelFeed
import com.example.jetpackcomposetrae20260119.data.FengTubeRepository
import com.example.jetpackcomposetrae20260119.data.FengTubeVideo
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FengTubeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FengTubeRepository(application)

    private val _feeds = MutableStateFlow<List<FengTubeChannelFeed>>(emptyList())
    val feeds: StateFlow<List<FengTubeChannelFeed>> = _feeds.asStateFlow()

    private val _recentVideos = MutableStateFlow<List<FengTubeVideo>>(emptyList())
    val recentVideos: StateFlow<List<FengTubeVideo>> = _recentVideos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        val cachedFeeds = repository.getCachedFeeds()
        setFeeds(cachedFeeds)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val loadedFeeds = repository.refreshFeeds()
            setFeeds(loadedFeeds)
            _errorMessage.value = if (loadedFeeds.all { it.videos.isEmpty() }) {
                "目前無法讀取鋒兄Tube頻道。"
            } else {
                null
            }
            _isLoading.value = false
        }
    }

    private fun setFeeds(feeds: List<FengTubeChannelFeed>) {
        _feeds.value = feeds
        _recentVideos.value = feeds
            .flatMap { it.videos }
            .filter { it.isWithinDays(3) }
            .sortedByDescending { runCatching { Instant.parse(it.publishedAt) }.getOrNull() }
    }

    private fun FengTubeVideo.isWithinDays(days: Long): Boolean {
        val published = runCatching { Instant.parse(publishedAt) }.getOrNull() ?: return false
        return Duration.between(published, Instant.now()).toDays() <= days
    }
}
