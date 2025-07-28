package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.remote.dto.analytics.*
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.AnalyticsRepository
import com.nicha.eventticketing.util.NetworkUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AnalyticsRepository {
    
    override fun getDailyRevenue(
        eventId: String?,
        startDate: String,
        endDate: String
    ): Flow<Resource<DailyRevenueResponseDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getDailyRevenue(eventId, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                val responseMap = response.body()!!
                
                val dailyRevenueResponse = DailyRevenueResponseDto(
                    dailyRevenue = responseMap["dailyRevenue"] as? Map<String, Double> ?: emptyMap(),
                    totalRevenue = (responseMap["totalRevenue"] as? Number)?.toDouble() ?: 0.0,
                    currencyCode = responseMap["currencyCode"] as? String ?: "VND",
                    startDate = responseMap["startDate"] as? String ?: startDate,
                    endDate = responseMap["endDate"] as? String ?: endDate
                )
                
                emit(Resource.Success(dailyRevenueResponse))
                Timber.d("Lấy dữ liệu doanh thu theo ngày thành công: ${dailyRevenueResponse.totalRevenue}")
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy dữ liệu doanh thu"))
                Timber.e("Lỗi API doanh thu: $errorMessage")
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi kết nối mạng"))
            Timber.e(e, "Exception khi lấy dữ liệu doanh thu")
        }
    }
    
    override fun getTicketSalesByType(
        eventId: String
    ): Flow<Resource<TicketSalesResponseDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getTicketSalesByType(eventId)
            if (response.isSuccessful && response.body() != null) {
                val responseMap = response.body()!!
                
                val ticketTypeDataMap = responseMap["ticketTypeData"] as? Map<String, Map<String, Any>> ?: emptyMap()
                val convertedTicketTypeData = ticketTypeDataMap.mapValues { (_, stats) ->
                    TicketTypeStatsDto(
                        count = (stats["count"] as? Number)?.toInt() ?: 0,
                        revenue = (stats["revenue"] as? Number)?.toDouble() ?: 0.0
                    )
                }
                
                val ticketTypeBreakdown = convertedTicketTypeData.mapValues { (_, stats) ->
                    stats.count
                }
                
                val ticketSalesResponse = TicketSalesResponseDto(
                    ticketTypeBreakdown = ticketTypeBreakdown,
                    totalRevenue = (responseMap["totalRevenue"] as? Number)?.toDouble() ?: 0.0,
                    dailySales = responseMap["dailySales"] as? Map<String, Int>
                )
                
                emit(Resource.Success(ticketSalesResponse))
                val totalSold = ticketTypeBreakdown.values.sum()
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy dữ liệu bán vé"))
                Timber.e("Lỗi API bán vé: $errorMessage")
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi kết nối mạng"))
            Timber.e(e, "Exception khi lấy dữ liệu bán vé")
        }
    }
    
    override fun getCheckInStatistics(
        eventId: String
    ): Flow<Resource<CheckInStatisticsDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getCheckInStatistics(eventId)
            if (response.isSuccessful && response.body() != null) {
                val responseMap = response.body()!!
                
                val ticketTypeBreakdownMap = responseMap["ticketTypeBreakdown"] as? Map<String, Map<String, Any>>
                val convertedBreakdown = ticketTypeBreakdownMap?.mapValues { (_, breakdown) ->
                    CheckInBreakdownDto(
                        total = (breakdown["total"] as? Number)?.toInt() ?: 0,
                        checkedIn = (breakdown["checkedIn"] as? Number)?.toInt() ?: 0,
                        notCheckedIn = (breakdown["notCheckedIn"] as? Number)?.toInt() ?: 0,
                        checkInRate = (breakdown["checkInRate"] as? Number)?.toDouble() ?: 0.0
                    )
                }
                
                val checkInStats = CheckInStatisticsDto(
                    totalTickets = (responseMap["totalTickets"] as? Number)?.toInt() ?: 0,
                    checkedIn = (responseMap["checkedIn"] as? Number)?.toInt() ?: 0,
                    notCheckedIn = (responseMap["notCheckedIn"] as? Number)?.toInt() ?: 0,
                    checkInRate = (responseMap["checkInRate"] as? Number)?.toDouble() ?: 0.0,
                    ticketTypeBreakdown = convertedBreakdown
                )
                
                emit(Resource.Success(checkInStats))
                Timber.d("Lấy thống kê check-in thành công: ${checkInStats.checkedIn}/${checkInStats.totalTickets}")
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy thống kê check-in"))
                Timber.e("Lỗi API check-in: $errorMessage")
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi kết nối mạng"))
            Timber.e(e, "Exception khi lấy thống kê check-in")
        }
    }
    
    override fun getRatingStatistics(
        eventId: String
    ): Flow<Resource<RatingStatisticsDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getRatingStatistics(eventId)
            if (response.isSuccessful && response.body() != null) {
                val ratingStats = response.body()!!
                emit(Resource.Success(ratingStats))
                Timber.d("Lấy thống kê đánh giá thành công: ${ratingStats.averageRating}/5.0")
            } else {
                val errorMessage = NetworkUtil.parseErrorResponse(response)
                emit(Resource.Error(errorMessage ?: "Không thể lấy thống kê đánh giá"))
                Timber.e("Lỗi API đánh giá: $errorMessage")
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi kết nối mạng"))
            Timber.e(e, "Exception khi lấy thống kê đánh giá")
        }
    }
    
    override fun getAnalyticsSummary(
        filter: AnalyticsFilterDto
    ): Flow<Resource<AnalyticsDashboardDto>> = flow {
        emit(Resource.Loading())
        try {
            // Gọi multiple APIs để tổng hợp dữ liệu
            val revenueFlow = getDailyRevenue(
                eventId = filter.eventIds?.firstOrNull(),
                startDate = filter.timeRange.startDate,
                endDate = filter.timeRange.endDate
            )
            
            // For now, return a basic summary
            // TODO: Implement proper aggregation logic
            val basicSummary = AnalyticsDashboardDto(
                totalRevenue = 0.0,
                totalTicketsSold = 0,
                totalCheckIns = 0,
                averageRating = 0.0,
                checkInRate = 0.0,
                period = filter.timeRange
            )
            
            emit(Resource.Success(basicSummary))
            Timber.d("Tạo tổng quan Analytics thành công")
            
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi khi tạo tổng quan Analytics"))
            Timber.e(e, "Exception khi tạo tổng quan Analytics")
        }
    }
    
    override fun exportAnalyticsToPdf(
        eventId: String?,
        startDate: String,
        endDate: String
    ): Flow<Resource<ByteArray>> = flow {
        emit(Resource.Loading())
        try {
            // TODO: Implement PDF export functionality
            emit(Resource.Error("Chức năng export PDF chưa được triển khai"))
            Timber.w("PDF export chưa được triển khai")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi khi export PDF"))
            Timber.e(e, "Exception khi export PDF")
        }
    }
    
    override fun exportAnalyticsToExcel(
        eventId: String?,
        startDate: String,
        endDate: String
    ): Flow<Resource<ByteArray>> = flow {
        emit(Resource.Loading())
        try {
            // TODO: Implement Excel export functionality
            emit(Resource.Error("Chức năng export Excel chưa được triển khai"))
            Timber.w("Excel export chưa được triển khai")
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi khi export Excel"))
            Timber.e(e, "Exception khi export Excel")
        }
    }
}
