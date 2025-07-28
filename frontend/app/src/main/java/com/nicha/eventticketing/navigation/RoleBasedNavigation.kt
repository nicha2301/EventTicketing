package com.nicha.eventticketing.navigation

import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import com.nicha.eventticketing.util.PermissionUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lớp quản lý điều hướng dựa trên role người dùng
 */
@Singleton
class RoleBasedNavigation @Inject constructor(
    private val permissionUtils: PermissionUtils
) {
    
    /**
     * Kiểm tra người dùng có thể truy cập vào điểm đến dành cho người tổ chức hay không
     * @param destination Điểm đến cần kiểm tra
     * @param currentUser Người dùng hiện tại
     * @return true nếu có quyền truy cập, false nếu không
     */
    fun canAccessDestination(destination: NavDestination, currentUser: UserDto?): Boolean {
        // Danh sách các điểm đến chỉ dành cho người tổ chức
        val organizerOnlyDestinations = listOf(
            NavDestination.EventDashboard,
            NavDestination.OrganizerEventDetail,
            NavDestination.EventImages,
            NavDestination.EventTicketTypes,
            NavDestination.OrganizerProfile,
            NavDestination.AnalyticsDashboard
        )
        
        // Nếu điểm đến không dành riêng cho người tổ chức, cho phép truy cập
        if (destination !in organizerOnlyDestinations) {
            return true
        }
        
        // Kiểm tra người dùng có phải là người tổ chức hoặc admin không
        return permissionUtils.isOrganizer(currentUser)
    }
    
    /**
     * Lấy điểm đến trang chủ phù hợp với vai trò của người dùng
     * @param user Người dùng đã đăng nhập
     * @return Điểm đến trang chủ phù hợp
     */
    fun getHomeDestinationForUser(user: UserDto?): NavDestination {
        return if (permissionUtils.isOrganizer(user)) {
            // Người tổ chức sẽ được đưa đến dashboard
            NavDestination.EventDashboard
        } else {
            // Người dùng thông thường sẽ được đưa đến trang chính
            NavDestination.Home
        }
    }
} 