package com.example.jetpackcomposetrae20260119.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcomposetrae20260119.data.OilPricePoint
import com.example.jetpackcomposetrae20260119.data.OilPriceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OilPriceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = OilPriceRepository(application)

    private val _history = MutableStateFlow<List<OilPricePoint>>(emptyList())
    val history: StateFlow<List<OilPricePoint>> = _history.asStateFlow()

    private val _latest = MutableStateFlow<OilPricePoint?>(null)
    val latest: StateFlow<OilPricePoint?> = _latest.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadSavedData()
        refreshLatestPrice()
    }

    fun refreshLatestPrice() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val latestPoint = repository.refreshLatestPrice()
            val savedHistory = repository.getSavedHistory()

            _history.value = savedHistory
            _latest.value = latestPoint ?: savedHistory.lastOrNull()
            _errorMessage.value = if (latestPoint == null && savedHistory.isEmpty()) {
                "目前無法取得 OQD 最新價格，請稍後再試。"
            } else {
                null
            }
            _isLoading.value = false
        }
    }

    private fun loadSavedData() {
        val savedHistory = repository.getSavedHistory()
        _history.value = savedHistory
        _latest.value = savedHistory.lastOrNull()
    }
}
