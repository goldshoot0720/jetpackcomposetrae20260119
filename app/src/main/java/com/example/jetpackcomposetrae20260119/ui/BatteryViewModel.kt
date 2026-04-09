package com.example.jetpackcomposetrae20260119.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcomposetrae20260119.data.BatteryRepository
import com.example.jetpackcomposetrae20260119.data.BatterySnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BatteryRepository(application)

    private val _snapshot = MutableStateFlow<BatterySnapshot?>(null)
    val snapshot: StateFlow<BatterySnapshot?> = _snapshot.asStateFlow()

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

            val result = repository.getBatterySnapshot()
            _snapshot.value = result
            _errorMessage.value = if (result == null) {
                "目前無法讀取電池資訊。"
            } else {
                null
            }

            _isLoading.value = false
        }
    }
}
