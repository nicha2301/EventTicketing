package com.nicha.eventticketing.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nicha.eventticketing.config.AppConfig
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import timber.log.Timber
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
        
        val CAMERA_PERMISSION = AppConfig.Permission.CAMERA_PERMISSION
        val STORAGE_READ_PERMISSION = AppConfig.Permission.STORAGE_READ_PERMISSION
        val MEDIA_IMAGES_PERMISSION = AppConfig.Permission.MEDIA_IMAGES_PERMISSION
        val NOTIFICATION_PERMISSION = AppConfig.Permission.NOTIFICATION_PERMISSION
        
        val REQUEST_CAMERA_PERMISSION = AppConfig.Permission.REQUEST_CAMERA_PERMISSION
        val REQUEST_STORAGE_PERMISSION = AppConfig.Permission.REQUEST_STORAGE_PERMISSION
        val REQUEST_NOTIFICATION_PERMISSION = AppConfig.Permission.REQUEST_NOTIFICATION_PERMISSION
        
        val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(NOTIFICATION_PERMISSION, CAMERA_PERMISSION, MEDIA_IMAGES_PERMISSION)
        } else {
            arrayOf(CAMERA_PERMISSION, STORAGE_READ_PERMISSION)
        }
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
     * Kiểm tra quyền thông báo đã được cấp chưa
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                NOTIFICATION_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Với Android dưới 13, không cần xin quyền thông báo một cách rõ ràng
            true
        }
    }
    
    /**
     * Kiểm tra tất cả quyền cần thiết đã được cấp chưa
     */
    fun hasAllRequiredPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Lấy danh sách quyền chưa được cấp
     */
    fun getNotGrantedPermissions(context: Context): List<String> {
        return REQUIRED_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Yêu cầu tất cả quyền cần thiết
     */
    fun requestAllPermissions(activity: Activity) {
        val notGrantedPermissions = getNotGrantedPermissions(activity)
        if (notGrantedPermissions.isNotEmpty()) {
            Timber.d("Đang yêu cầu các quyền: ${notGrantedPermissions.joinToString()}")
            ActivityCompat.requestPermissions(
                activity,
                notGrantedPermissions.toTypedArray(),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }
    
    /**
     * Mở cài đặt ứng dụng để người dùng cấp quyền thủ công
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", context.packageName, null)
        context.startActivity(intent)
    }
    
    /**
     * Yêu cầu quyền camera
     */
    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(CAMERA_PERMISSION),
            REQUEST_CAMERA_PERMISSION
        )
    }
} 