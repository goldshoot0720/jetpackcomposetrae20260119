package com.example.jetpackcomposetrae20260119.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcomposetrae20260119.data.AppwriteRepository
import com.example.jetpackcomposetrae20260119.data.Subscription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppwriteRepository(application)
    
    private val _subscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptions: StateFlow<List<Subscription>> = _subscriptions.asStateFlow()
    
    private val _upcomingSubscriptions = MutableStateFlow<List<Subscription>>(emptyList())
    val upcomingSubscriptions: StateFlow<List<Subscription>> = _upcomingSubscriptions.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkConnectivity()
        loadSubscriptions()
    }

    private fun checkConnectivity() {
        viewModelScope.launch {
            repository.ping()
        }
    }

    fun loadSubscriptions() {
        viewModelScope.launch {
            _isLoading.value = true
            val allSubs = repository.getSubscriptions()
            _subscriptions.value = allSubs
            
            // Filter upcoming (within 3 days) for in-app notification
            val today = LocalDate.now()
            val limitDate = today.plusDays(3)
            _upcomingSubscriptions.value = allSubs.filter {
                try {
                    if (it.nextDate.isBlank()) return@filter false
                    val itemDate = ZonedDateTime.parse(it.nextDate).toLocalDate()
                    !itemDate.isBefore(today) && !itemDate.isAfter(limitDate)
                } catch (e: Exception) {
                    false
                }
            }
            
            _isLoading.value = false
        }
    }

    fun addSubscription(name: String, price: Int, nextDate: String, site: String = "", note: String = "", account: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            val newSubscription = Subscription(
                id = "",
                name = name,
                site = site,
                price = price,
                nextDate = nextDate,
                note = note,
                account = account,
                createdAt = "",
                updatedAt = ""
            )
            repository.addSubscription(newSubscription)
            loadSubscriptions()
        }
    }
}
