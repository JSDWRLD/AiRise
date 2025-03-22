package com.teamnotfound.airise.health

import com.teamnotfound.airise.health.HealthData
import com.teamnotfound.airise.health.HealthDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Shared viewmodel for IOS and Android
class HealthDashboardViewModel(
    // Saving provider and scope
    private val provider: HealthDataProvider,
    private val scope: CoroutineScope = MainScope()
) {
    private val _healthData = MutableStateFlow<HealthData?>(null)
    val healthData: StateFlow<HealthData?> = _healthData

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun requestAndLoadData() {
        _isLoading.value = true
        scope.launch {
            try {
                val granted = provider.requestPermissions()
                if (!granted) {
                    _error.value = "Permission denied by user."
                    return@launch
                }

                val data = provider.getHealthData()
                _healthData.value = data
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}