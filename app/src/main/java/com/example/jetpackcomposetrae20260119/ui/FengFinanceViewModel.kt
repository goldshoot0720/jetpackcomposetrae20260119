package com.example.jetpackcomposetrae20260119.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcomposetrae20260119.data.FengFinanceQuote
import com.example.jetpackcomposetrae20260119.data.FengFinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FengFinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FengFinanceRepository(application)

    private val _quotes = MutableStateFlow<List<FengFinanceQuote>>(emptyList())
    val quotes: StateFlow<List<FengFinanceQuote>> = _quotes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        _quotes.value = repository.getCachedQuotes()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val loadedQuotes = repository.refreshQuotes()
            _quotes.value = loadedQuotes
            _errorMessage.value = if (loadedQuotes.all { it.numericPrice == null }) {
                "暫時讀不到 CNBC 金融報價，請稍後再試。"
            } else {
                null
            }
            _isLoading.value = false
        }
    }
}
