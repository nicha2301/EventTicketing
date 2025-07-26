package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.local.dao.TicketDao
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.ticket.CheckInRequestDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseResponseDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseRequestDto
import com.nicha.eventticketing.data.remote.dto.ticket.PendingOrderDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.mapper.TicketMapper
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.TicketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.nicha.eventticketing.data.remote.dto.event.PageableDto
import com.nicha.eventticketing.data.remote.dto.event.SortDto
import com.nicha.eventticketing.util.NetworkUtil

@Singleton
class TicketRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val ticketDao: TicketDao
) : TicketRepository {
    var isOnline: Boolean = true
        set(value) {
            if (field != value) {
                field = value
            }
        }
    
    override fun purchaseTickets(purchaseRequest: TicketPurchaseRequestDto): Flow<Resource<TicketPurchaseResponseDto>> = flow {
        emit(Resource.Loading())
        
        try {
            if (!NetworkUtil.isNetworkAvailable()) {
                emit(Resource.Error("Không có kết nối mạng"))
                return@flow
            }
            
            val response = apiService.purchaseTickets(purchaseRequest)
            
            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success && apiResponse.data != null) {
                        emit(Resource.Success(apiResponse.data))
                    } else {
                        val errorMsg = apiResponse.message ?: "Mua vé thất bại"
                        emit(Resource.Error(errorMsg))
                    }
                } ?: run {
                    emit(Resource.Error("Phản hồi từ server không hợp lệ"))
                }
            } else {
                val errorMsg = "Mua vé thất bại: ${response.code()} - ${response.message()}"
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Lỗi khi mua vé: ${e.message}"
            emit(Resource.Error(errorMsg))
        }
    }
    
    override fun getTicketById(ticketId: String): Flow<Resource<TicketDto>> = flow {
        emit(Resource.Loading())
        
        val actuallyOnline = isOnline && NetworkUtil.isActuallyConnected()
        
        if (actuallyOnline) {
            try {
                val response = apiService.getTicketById(ticketId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val ticket = response.body()?.data
                    if (ticket != null) {
                        withContext(Dispatchers.IO) { ticketDao.insertTicket(TicketMapper.dtoToEntity(ticket)) }
                        emit(Resource.Success(ticket))
                    } else {
                        emit(Resource.Error("Không tìm thấy vé"))
                    }
                } else {
                    emit(Resource.Error(response.body()?.message ?: "Không thể lấy thông tin vé"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
            }
        } else {
            val localTicket = withContext(Dispatchers.IO) { ticketDao.getTicketById(ticketId) }
            if (localTicket != null) {
                emit(Resource.Success(TicketMapper.entityToDto(localTicket)))
            } else {
                emit(Resource.Error("Không tìm thấy vé trong cache"))
            }
        }
    }
    
    override fun getTicketByNumber(ticketNumber: String): Flow<Resource<TicketDto>> = flow {
        emit(Resource.Loading())
        
        val actuallyOnline = isOnline && NetworkUtil.isActuallyConnected()
        
        if (actuallyOnline) {
            try {
                val response = apiService.getTicketByNumber(ticketNumber)
                if (response.isSuccessful && response.body()?.success == true) {
                    val ticket = response.body()?.data
                    if (ticket != null) {
                        withContext(Dispatchers.IO) { ticketDao.insertTicket(TicketMapper.dtoToEntity(ticket)) }
                        emit(Resource.Success(ticket))
                    } else {
                        emit(Resource.Error("Không tìm thấy vé"))
                    }
                } else {
                    emit(Resource.Error(response.body()?.message ?: "Không thể lấy thông tin vé"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
            }
        } else {
            // Lấy từ Room (theo ticketNumber)
            val localTickets = withContext(Dispatchers.IO) { ticketDao.getAllTickets() }
            val ticket = localTickets.find { it.ticketNumber == ticketNumber }
            if (ticket != null) {
                emit(Resource.Success(TicketMapper.entityToDto(ticket)))
            } else {
                emit(Resource.Error("Không tìm thấy vé trong cache"))
            }
        }
    }
    
    override fun getMyTickets(
        status: String?, 
        page: Int, 
        size: Int
    ): Flow<Resource<PageDto<TicketDto>>> = flow {
        emit(Resource.Loading())
        
        val actuallyOnline = isOnline && NetworkUtil.isActuallyConnected()
        
        if (actuallyOnline) {
            try {
                val response = apiService.getMyTickets(status, page, size)
                if (response.isSuccessful && response.body()?.success == true) {
                    val tickets = response.body()?.data
                    if (tickets != null) {
                        val entities = tickets.content.map { TicketMapper.dtoToEntity(it) }
                        withContext(Dispatchers.IO) { ticketDao.insertTickets(entities) }
                        emit(Resource.Success(tickets))
                    } else {
                        emit(Resource.Error("Không tìm thấy vé"))
                    }
                } else {
                    emit(Resource.Error(response.body()?.message ?: "Không thể lấy danh sách vé"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
            }
        } else {
            val localTickets = withContext(Dispatchers.IO) { ticketDao.getAllTickets() }
            val dtos = localTickets.map { TicketMapper.entityToDto(it) }
            emit(Resource.Success(PageDto(
                content = dtos,
                pageable = PageableDto(
                    pageNumber = 0,
                    pageSize = dtos.size,
                    sort = SortDto(sorted = false, unsorted = true, empty = true),
                    offset = 0L,
                    paged = false,
                    unpaged = true
                ),
                totalPages = 1,
                totalElements = dtos.size.toLong(),
                last = true,
                size = dtos.size,
                number = 0,
                sort = SortDto(sorted = false, unsorted = true, empty = true),
                numberOfElements = dtos.size,
                first = true,
                empty = dtos.isEmpty()
            )))
        }
    }
    
    override fun getMyPendingTickets(): Flow<Resource<List<PendingOrderDto>>> = flow {
        emit(Resource.Loading())
        if (isOnline) {
            try {
                val response = apiService.getMyPendingTickets()
                if (response.isSuccessful && response.body()?.success == true) {
                    val pendingOrders = response.body()?.data
                    if (pendingOrders != null) {
                        emit(Resource.Success(pendingOrders))
                    } else {
                        emit(Resource.Success(emptyList()))
                    }
                } else {
                    emit(Resource.Error(response.body()?.message ?: "Không thể lấy danh sách đơn hàng pending"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
            }
        } else {
            emit(Resource.Success(emptyList()))
        }
    }
    
    override fun checkInTicket(request: CheckInRequestDto): Flow<Resource<TicketDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.checkInTicket(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val ticket = response.body()?.data
                if (ticket != null) {
                    withContext(Dispatchers.IO) { ticketDao.insertTicket(TicketMapper.dtoToEntity(ticket)) }
                    emit(Resource.Success(ticket))
                } else {
                    emit(Resource.Error("Không thể check-in vé"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Check-in vé thất bại"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun cancelTicket(ticketId: String): Flow<Resource<TicketDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.cancelTicket(ticketId)
            if (response.isSuccessful && response.body()?.success == true) {
                val ticket = response.body()?.data
                if (ticket != null) {
                    withContext(Dispatchers.IO) { ticketDao.insertTicket(TicketMapper.dtoToEntity(ticket)) }
                    emit(Resource.Success(ticket))
                } else {
                    emit(Resource.Error("Không thể hủy vé"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Hủy vé thất bại"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getEventTickets(
        eventId: String, 
        status: String?, 
        page: Int, 
        size: Int
    ): Flow<Resource<PageDto<TicketDto>>> = flow {
        emit(Resource.Loading())
        try {
            // API hiện tại không có endpoint riêng để lấy danh sách vé của sự kiện
            // Tạm thời chưa triển khai
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun resendTicketConfirmation(ticketId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            // API hiện tại không có endpoint riêng để gửi lại email xác nhận vé
            // Tạm thời chưa triển khai
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun validateTicket(ticketId: String, code: String): Flow<Resource<TicketDto>> = flow {
        emit(Resource.Loading())
        try {
            // API hiện tại không có endpoint riêng để xác thực vé
            // Tạm thời chưa triển khai
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun transferTicket(ticketId: String, email: String): Flow<Resource<TicketDto>> = flow {
        emit(Resource.Loading())
        try {
            // API hiện tại không có endpoint riêng để chuyển nhượng vé
            // Tạm thời chưa triển khai
            emit(Resource.Error("Chức năng chưa được hỗ trợ"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
} 