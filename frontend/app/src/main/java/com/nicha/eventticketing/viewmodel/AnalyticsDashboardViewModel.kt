package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.analytics.*
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.domain.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AnalyticsDashboardViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(AnalyticsDashboardUiState())
    val uiState: StateFlow<AnalyticsDashboardUiState> = _uiState.asStateFlow()
    
    // Data States
    private val _dailyRevenueState = MutableStateFlow<ResourceState<DailyRevenueResponseDto>>(ResourceState.Initial)
    val dailyRevenueState: StateFlow<ResourceState<DailyRevenueResponseDto>> = _dailyRevenueState.asStateFlow()
    
    private val _ticketSalesState = MutableStateFlow<ResourceState<TicketSalesResponseDto>>(ResourceState.Initial)
    val ticketSalesState: StateFlow<ResourceState<TicketSalesResponseDto>> = _ticketSalesState.asStateFlow()
    
    private val _checkInStatsState = MutableStateFlow<ResourceState<CheckInStatisticsDto>>(ResourceState.Initial)
    val checkInStatsState: StateFlow<ResourceState<CheckInStatisticsDto>> = _checkInStatsState.asStateFlow()
    
    private val _ratingStatsState = MutableStateFlow<ResourceState<RatingStatisticsDto>>(ResourceState.Initial)
    val ratingStatsState: StateFlow<ResourceState<RatingStatisticsDto>> = _ratingStatsState.asStateFlow()

    init {
        // Initialize with default date range (last 30 days)
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(30)
        
        updateDateRange(
            startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        )
        
        loadAnalyticsData()
    }

    fun updateDateRange(startDate: String, endDate: String) {
        _uiState.value = _uiState.value.copy(
            selectedDateRange = startDate to endDate
        )
    }

    fun updateSelectedPeriod(period: String) {
        _uiState.value = _uiState.value.copy(
            selectedPeriod = period
        )
    }

    fun updateSelectedEvents(eventIds: List<String>) {
        _uiState.value = _uiState.value.copy(
            selectedEvents = eventIds
        )
    }

    fun updateSelectedEventForDetails(eventId: String?) {
        _uiState.value = _uiState.value.copy(
            selectedEventForDetails = eventId
        )
        
        // Reload data for specific event
        if (eventId != null) {
            loadEventSpecificData(eventId)
        }
    }

    fun refreshData() {
        loadAnalyticsData()
    }

    fun exportData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExporting = true)
                
                // TODO: Implement export functionality
                Timber.d("Export functionality to be implemented")
                
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportMessage = "Export chức năng sẽ được triển khai sau"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportMessage = "Lỗi khi export: ${e.message}"
                )
                Timber.e(e, "Error exporting analytics data")
            }
        }
    }

    fun clearExportMessage() {
        _uiState.value = _uiState.value.copy(exportMessage = null)
    }

    private fun loadAnalyticsData() {
        val currentState = _uiState.value
        val eventId = currentState.selectedEventForDetails
        
        loadDailyRevenue(eventId)
        
        if (eventId != null) {
            loadEventSpecificData(eventId)
        }
    }

    private fun loadDailyRevenue(eventId: String? = null) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val (startDate, endDate) = currentState.selectedDateRange
            
            analyticsRepository.getDailyRevenue(eventId, startDate, endDate).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _dailyRevenueState.value = ResourceState.Loading
                    }
                    is Resource.Success -> {
                        _dailyRevenueState.value = ResourceState.Success(result.data!!)
                        Timber.d("Daily revenue loaded successfully")
                    }
                    is Resource.Error -> {
                        _dailyRevenueState.value = ResourceState.Error(result.message ?: "Unknown error")
                        Timber.e("Failed to load daily revenue: ${result.message}")
                    }
                }
            }
        }
    }

    private fun loadEventSpecificData(eventId: String) {
        loadTicketSales(eventId)
        loadCheckInStats(eventId)
        loadRatingStats(eventId)
    }

    private fun loadTicketSales(eventId: String) {
        viewModelScope.launch {
            analyticsRepository.getTicketSalesByType(eventId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _ticketSalesState.value = ResourceState.Loading
                    }
                    is Resource.Success -> {
                        _ticketSalesState.value = ResourceState.Success(result.data!!)
                        Timber.d("Ticket sales loaded successfully")
                    }
                    is Resource.Error -> {
                        _ticketSalesState.value = ResourceState.Error(result.message ?: "Unknown error")
                        Timber.e("Failed to load ticket sales: ${result.message}")
                    }
                }
            }
        }
    }

    private fun loadCheckInStats(eventId: String) {
        viewModelScope.launch {
            analyticsRepository.getCheckInStatistics(eventId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _checkInStatsState.value = ResourceState.Loading
                    }
                    is Resource.Success -> {
                        _checkInStatsState.value = ResourceState.Success(result.data!!)
                        Timber.d("Check-in stats loaded successfully")
                    }
                    is Resource.Error -> {
                        _checkInStatsState.value = ResourceState.Error(result.message ?: "Unknown error")
                        Timber.e("Failed to load check-in stats: ${result.message}")
                    }
                }
            }
        }
    }

    private fun loadRatingStats(eventId: String) {
        viewModelScope.launch {
            analyticsRepository.getRatingStatistics(eventId).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _ratingStatsState.value = ResourceState.Loading
                    }
                    is Resource.Success -> {
                        _ratingStatsState.value = ResourceState.Success(result.data!!)
                        Timber.d("Rating stats loaded successfully")
                    }
                    is Resource.Error -> {
                        _ratingStatsState.value = ResourceState.Error(result.message ?: "Unknown error")
                        Timber.e("Failed to load rating stats: ${result.message}")
                    }
                }
            }
        }
    }
}

/**
 * UI State for Analytics Dashboard
 */
data class AnalyticsDashboardUiState(
    val selectedDateRange: Pair<String, String> = "" to "",
    val selectedPeriod: String = "DAILY",
    val selectedEvents: List<String> = emptyList(),
    val selectedEventForDetails: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val error: String? = null
)
