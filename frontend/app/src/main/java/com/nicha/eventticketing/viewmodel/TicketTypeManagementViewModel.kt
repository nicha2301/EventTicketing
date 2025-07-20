package com.nicha.eventticketing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto
import com.nicha.eventticketing.domain.model.Resource
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.domain.repository.TicketTypeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TicketTypeManagementViewModel @Inject constructor(
    private val ticketTypeRepository: TicketTypeRepository
) : ViewModel() {
    
    // State cho danh sách loại vé của sự kiện
    private val _ticketTypesState = MutableStateFlow<ResourceState<List<TicketTypeDto>>>(ResourceState.Initial)
    val ticketTypesState: StateFlow<ResourceState<List<TicketTypeDto>>> = _ticketTypesState.asStateFlow()
    
    // State cho việc tạo loại vé mới
    private val _createTicketTypeState = MutableStateFlow<ResourceState<TicketTypeDto>>(ResourceState.Initial)
    val createTicketTypeState: StateFlow<ResourceState<TicketTypeDto>> = _createTicketTypeState.asStateFlow()
    
    // State cho việc cập nhật loại vé
    private val _updateTicketTypeState = MutableStateFlow<ResourceState<TicketTypeDto>>(ResourceState.Initial)
    val updateTicketTypeState: StateFlow<ResourceState<TicketTypeDto>> = _updateTicketTypeState.asStateFlow()
    
    // State cho việc xóa loại vé
    private val _deleteTicketTypeState = MutableStateFlow<ResourceState<Boolean>>(ResourceState.Initial)
    val deleteTicketTypeState: StateFlow<ResourceState<Boolean>> = _deleteTicketTypeState.asStateFlow()
    
    /**
     * Lấy danh sách loại vé của sự kiện
     */
    fun getTicketTypes(eventId: String) {
        _ticketTypesState.value = ResourceState.Loading
        
        viewModelScope.launch {
            ticketTypeRepository.getTicketTypes(eventId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val pageResponse = result.data
                        if (pageResponse != null) {
                            val ticketTypes = pageResponse.content
                            _ticketTypesState.value = ResourceState.Success(ticketTypes)
                            Timber.d("Lấy danh sách loại vé thành công: ${ticketTypes.size} loại vé")
                        } else {
                            Timber.e("Không tìm thấy loại vé")
                            _ticketTypesState.value = ResourceState.Error("Không tìm thấy loại vé")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Lấy danh sách loại vé thất bại: ${result.message}")
                        _ticketTypesState.value = ResourceState.Error(result.message ?: "Không thể lấy danh sách loại vé")
                    }
                    is Resource.Loading -> {
                        _ticketTypesState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Tạo loại vé mới
     */
    fun createTicketType(ticketType: TicketTypeDto) {
        _createTicketTypeState.value = ResourceState.Loading
        
        viewModelScope.launch {
            ticketTypeRepository.createTicketType(ticketType.eventId, ticketType).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val createdTicketType = result.data
                        if (createdTicketType != null) {
                            _createTicketTypeState.value = ResourceState.Success(createdTicketType)
                            Timber.d("Tạo loại vé thành công: ${createdTicketType.id}")
                        } else {
                            Timber.e("Không thể tạo loại vé")
                            _createTicketTypeState.value = ResourceState.Error("Không thể tạo loại vé")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Tạo loại vé thất bại: ${result.message}")
                        _createTicketTypeState.value = ResourceState.Error(result.message ?: "Không thể tạo loại vé")
                    }
                    is Resource.Loading -> {
                        _createTicketTypeState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Cập nhật loại vé
     */
    fun updateTicketType(ticketType: TicketTypeDto) {
        _updateTicketTypeState.value = ResourceState.Loading
        
        viewModelScope.launch {
            ticketTypeRepository.updateTicketType(ticketType.id, ticketType).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val updatedTicketType = result.data
                        if (updatedTicketType != null) {
                            _updateTicketTypeState.value = ResourceState.Success(updatedTicketType)
                            Timber.d("Cập nhật loại vé thành công: ${updatedTicketType.id}")
                        } else {
                            Timber.e("Không thể cập nhật loại vé")
                            _updateTicketTypeState.value = ResourceState.Error("Không thể cập nhật loại vé")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Cập nhật loại vé thất bại: ${result.message}")
                        _updateTicketTypeState.value = ResourceState.Error(result.message ?: "Không thể cập nhật loại vé")
                    }
                    is Resource.Loading -> {
                        _updateTicketTypeState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Xóa loại vé
     */
    fun deleteTicketType(ticketTypeId: String, eventId: String) {
        _deleteTicketTypeState.value = ResourceState.Loading
        
        viewModelScope.launch {
            ticketTypeRepository.deleteTicketType(ticketTypeId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val success = result.data
                        if (success == true) {
                            _deleteTicketTypeState.value = ResourceState.Success(true)
                            Timber.d("Xóa loại vé thành công: $ticketTypeId")
                            
                            // Cập nhật lại danh sách loại vé
                            getTicketTypes(eventId)
                        } else {
                            Timber.e("Không thể xóa loại vé")
                            _deleteTicketTypeState.value = ResourceState.Error("Không thể xóa loại vé")
                        }
                    }
                    is Resource.Error -> {
                        Timber.e("Xóa loại vé thất bại: ${result.message}")
                        _deleteTicketTypeState.value = ResourceState.Error(result.message ?: "Không thể xóa loại vé")
                    }
                    is Resource.Loading -> {
                        _deleteTicketTypeState.value = ResourceState.Loading
                    }
                }
            }
        }
    }
    
    /**
     * Reset states
     */
    fun resetCreateTicketTypeState() {
        _createTicketTypeState.value = ResourceState.Initial
    }
    
    fun resetUpdateTicketTypeState() {
        _updateTicketTypeState.value = ResourceState.Initial
    }
    
    fun resetDeleteTicketTypeState() {
        _deleteTicketTypeState.value = ResourceState.Initial
    }
} 