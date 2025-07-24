package com.nicha.eventticketing.data.repository

import com.nicha.eventticketing.data.local.dao.TicketDao
import com.nicha.eventticketing.data.remote.dto.event.PageDto
import com.nicha.eventticketing.data.remote.dto.ticket.CheckInRequestDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketPurchaseResponseDto
import com.nicha.eventticketing.data.remote.service.ApiService
import com.nicha.eventticketing.domain.mapper.TicketMapper
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.repository.TicketRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
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
    // isOnline sẽ được truyền từ ViewModel/UI xuống repository
    var isOnline: Boolean = true
        set(value) {
            if (field != value) {
                Timber.d("[TicketRepository] Trạng thái mạng thay đổi: ${if (value) "ONLINE" else "OFFLINE"}")
                field = value
            }
        }
    
    override fun purchaseTickets(purchaseDto: TicketPurchaseDto): Flow<Resource<TicketPurchaseResponseDto>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.purchaseTickets(purchaseDto)
            if (response.isSuccessful && response.body()?.success == true) {
                val purchaseResponse = response.body()?.data
                if (purchaseResponse != null) {
                    // Sau khi mua vé thành công, có thể fetch lại danh sách vé để đồng bộ local
                    emit(Resource.Success(purchaseResponse))
                } else {
                    emit(Resource.Error("Không thể hoàn tất việc mua vé"))
                }
            } else {
                emit(Resource.Error(response.body()?.message ?: "Mua vé thất bại"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi mua vé cho sự kiện: ${purchaseDto.eventId}")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
    
    override fun getTicketById(ticketId: String): Flow<Resource<TicketDto>> = flow {
        emit(Resource.Loading())
        
        // Kiểm tra trạng thái mạng trước khi quyết định nguồn dữ liệu
        val actuallyOnline = isOnline && NetworkUtil.isActuallyConnected()
        Timber.d("[TicketRepository] Trạng thái mạng: isOnline=$isOnline, actuallyOnline=$actuallyOnline")
        
        if (actuallyOnline) {
            Timber.d("[TicketRepository] ONLINE: Lấy chi tiết vé từ API")
            try {
                val response = apiService.getTicketById(ticketId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val ticket = response.body()?.data
                    if (ticket != null) {
                        withContext(Dispatchers.IO) { ticketDao.insertTicket(TicketMapper.dtoToEntity(ticket)) }
                        Timber.d("[TicketRepository] Đã lưu vé %s vào cache Room", ticket.id)
                        emit(Resource.Success(ticket))
                    } else {
                        emit(Resource.Error("Không tìm thấy vé"))
                    }
                } else {
                    emit(Resource.Error(response.body()?.message ?: "Không thể lấy thông tin vé"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi lấy thông tin vé: $ticketId")
                emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
            }
        } else {
            Timber.d("[TicketRepository] OFFLINE: Lấy chi tiết vé từ cache Room")
            val localTicket = withContext(Dispatchers.IO) { ticketDao.getTicketById(ticketId) }
            if (localTicket != null) {
                Timber.d("[TicketRepository] Đã lấy vé %s từ cache Room", localTicket.id)
                emit(Resource.Success(TicketMapper.entityToDto(localTicket)))
            } else {
                Timber.d("[TicketRepository] Không tìm thấy vé %s trong cache Room", ticketId)
                emit(Resource.Error("Không tìm thấy vé trong cache"))
            }
        }
    }
    
    override fun getTicketByNumber(ticketNumber: String): Flow<Resource<TicketDto>> = flow {
        emit(Resource.Loading())
        
        // Kiểm tra trạng thái mạng trước khi quyết định nguồn dữ liệu
        val actuallyOnline = isOnline && NetworkUtil.isActuallyConnected()
        Timber.d("[TicketRepository] Trạng thái mạng: isOnline=$isOnline, actuallyOnline=$actuallyOnline")
        
        if (actuallyOnline) {
            Timber.d("[TicketRepository] ONLINE: Lấy chi tiết vé theo số vé từ API")
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
                Timber.e(e, "Lỗi khi lấy thông tin vé theo số vé: $ticketNumber")
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
        
        // Kiểm tra trạng thái mạng trước khi quyết định nguồn dữ liệu
        val actuallyOnline = isOnline && NetworkUtil.isActuallyConnected()
        Timber.d("[TicketRepository] Trạng thái mạng: isOnline=$isOnline, actuallyOnline=$actuallyOnline")
        
        if (actuallyOnline) {
            Timber.d("[TicketRepository] ONLINE: Lấy danh sách vé từ API")
            try {
                val response = apiService.getMyTickets(status, page, size)
                if (response.isSuccessful && response.body()?.success == true) {
                    val tickets = response.body()?.data
                    if (tickets != null) {
                        val entities = tickets.content.map { TicketMapper.dtoToEntity(it) }
                        withContext(Dispatchers.IO) { ticketDao.insertTickets(entities) }
                        Timber.d("[TicketRepository] Đã lưu %d vé vào cache Room", entities.size)
                        emit(Resource.Success(tickets))
                    } else {
                        emit(Resource.Error("Không tìm thấy vé"))
                    }
                } else {
                    emit(Resource.Error(response.body()?.message ?: "Không thể lấy danh sách vé"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Lỗi khi lấy danh sách vé của người dùng hiện tại")
                emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
            }
        } else {
            Timber.d("[TicketRepository] OFFLINE: Lấy danh sách vé từ cache Room")
            val localTickets = withContext(Dispatchers.IO) { ticketDao.getAllTickets() }
            val dtos = localTickets.map { TicketMapper.entityToDto(it) }
            Timber.d("[TicketRepository] Đã lấy %d vé từ cache Room", dtos.size)
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
            Timber.e(e, "Lỗi khi check-in vé: ${request.ticketId}")
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
            Timber.e(e, "Lỗi khi hủy vé: $ticketId")
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
            Timber.e(e, "Lỗi khi lấy danh sách vé của sự kiện: $eventId")
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
            Timber.e(e, "Lỗi khi gửi lại email xác nhận vé: $ticketId")
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
            Timber.e(e, "Lỗi khi xác thực vé: $ticketId")
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
            Timber.e(e, "Lỗi khi chuyển nhượng vé: $ticketId")
            emit(Resource.Error(e.message ?: "Đã xảy ra lỗi không xác định"))
        }
    }
} 