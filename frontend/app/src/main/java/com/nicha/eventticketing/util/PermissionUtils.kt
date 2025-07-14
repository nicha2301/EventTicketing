package com.nicha.eventticketing.util

import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lớp tiện ích để kiểm tra quyền người dùng
 */
@Singleton
class PermissionUtils @Inject constructor() {
    
    companion object {
        const val ROLE_USER = "USER"
        const val ROLE_ORGANIZER = "ORGANIZER"
        const val ROLE_ADMIN = "ADMIN"
    }
    
    /**
     * Kiểm tra người dùng có phải là admin hay không
     */
    fun isAdmin(user: UserDto?): Boolean {
        return user?.role == ROLE_ADMIN
    }
    
    /**
     * Kiểm tra người dùng có phải là organizer hay không
     */
    fun isOrganizer(user: UserDto?): Boolean {
        return user?.role == ROLE_ORGANIZER || isAdmin(user)
    }
    
    /**
     * Kiểm tra người dùng có thể tạo sự kiện hay không
     */
    fun canCreateEvent(user: UserDto?): Boolean {
        return isOrganizer(user)
    }
    
    /**
     * Kiểm tra người dùng có thể chỉnh sửa sự kiện hay không
     * @param user Người dùng hiện tại
     * @param organizerId ID của người tạo sự kiện
     */
    fun canEditEvent(user: UserDto?, organizerId: String): Boolean {
        return isAdmin(user) || (isOrganizer(user) && user?.id == organizerId)
    }
    
    /**
     * Kiểm tra người dùng có thể xóa sự kiện hay không
     * @param user Người dùng hiện tại
     * @param organizerId ID của người tạo sự kiện
     */
    fun canDeleteEvent(user: UserDto?, organizerId: String): Boolean {
        return isAdmin(user) || (isOrganizer(user) && user?.id == organizerId)
    }
    
    /**
     * Kiểm tra người dùng có thể quản lý người dùng khác hay không
     */
    fun canManageUsers(user: UserDto?): Boolean {
        return isAdmin(user)
    }
    
    /**
     * Kiểm tra người dùng có thể xem báo cáo hay không
     */
    fun canViewReports(user: UserDto?): Boolean {
        return isAdmin(user) || isOrganizer(user)
    }
    
    /**
     * Kiểm tra người dùng có thể xem thống kê chi tiết hay không
     */
    fun canViewDetailedAnalytics(user: UserDto?): Boolean {
        return isAdmin(user) || isOrganizer(user)
    }
} 