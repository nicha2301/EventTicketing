package com.nicha.eventticketing.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        
        // Permission constants
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        const val STORAGE_READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
        const val MEDIA_IMAGES_PERMISSION = Manifest.permission.READ_MEDIA_IMAGES
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
    
    /**
     * Kiểm tra quyền camera đã được cấp chưa
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            CAMERA_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Kiểm tra quyền đọc ảnh đã được cấp chưa
     */
    fun hasReadImagesPermission(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                MEDIA_IMAGES_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                STORAGE_READ_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Yêu cầu quyền camera
     */
    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(CAMERA_PERMISSION),
            100
        )
    }
} 