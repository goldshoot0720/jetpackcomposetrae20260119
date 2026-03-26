package com.example.jetpackcomposetrae20260119.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcomposetrae20260119.data.USDebtPoint
import com.example.jetpackcomposetrae20260119.data.USDebtRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class USDebtViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = USDebtRepository(application)

    private val _history = MutableStateFlow<List<USDebtPoint>>(emptyList())
    val history: StateFlow<List<USDebtPoint>> = _history.asStateFlow()

    private val _latest = MutableStateFlow<USDebtPoint?>(null)
    val latest: StateFlow<USDebtPoint?> = _latest.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadSavedData()
        refreshNationalDebt()
    }

    fun refreshNationalDebt() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val latestPoint = repository.refreshNationalDebt()
            val savedHistory = repository.getSavedHistory()
            val fallbackPoint = savedHistory.lastOrNull()

            _history.value = savedHistory
            _latest.value = latestPoint ?: fallbackPoint
            _errorMessage.value = when {
                latestPoint != null -> null
                fallbackPoint != null -> "目前無法取得最新 US National Debt，已先顯示上次成功抓取的資料。"
                else -> "目前無法取得 US National Debt，請稍後再試。"
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
