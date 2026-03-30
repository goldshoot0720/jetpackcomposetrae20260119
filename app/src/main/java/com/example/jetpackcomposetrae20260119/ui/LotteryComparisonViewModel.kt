package com.example.jetpackcomposetrae20260119.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcomposetrae20260119.data.LotteryComparisonRepository
import com.example.jetpackcomposetrae20260119.data.LotteryDashboard
import com.example.jetpackcomposetrae20260119.data.LotterySection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LotteryComparisonViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LotteryComparisonRepository()

    private val _sections = MutableStateFlow<List<LotterySection>>(emptyList())
    val sections: StateFlow<List<LotterySection>> = _sections.asStateFlow()

    private val _rangeLabel = MutableStateFlow("")
    val rangeLabel: StateFlow<String> = _rangeLabel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            runCatching {
                repository.fetchDashboard()
            }.onSuccess { dashboard: LotteryDashboard ->
                _sections.value = dashboard.sections
                _rangeLabel.value = "官方資料區間：${dashboard.rangeStartMonth} 至 ${dashboard.rangeEndMonth}"
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "讀取台彩資料失敗，請稍後再試。"
            }

            _isLoading.value = false
        }
    }
}
