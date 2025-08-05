package com.nicha.eventticketing

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.nicha.eventticketing.config.AppConfig
import com.nicha.eventticketing.data.notification.EventTicketingFirebaseMessagingService
import com.nicha.eventticketing.data.preferences.PreferencesManager
import com.nicha.eventticketing.domain.service.FcmTokenService
import com.nicha.eventticketing.navigation.NavDestination
import com.nicha.eventticketing.navigation.NavGraph
import com.nicha.eventticketing.navigation.RoleBasedNavigation
import com.nicha.eventticketing.ui.theme.EventTicketingTheme
import com.nicha.eventticketing.viewmodel.AuthState
import com.nicha.eventticketing.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.nicha.eventticketing.util.PermissionUtils
import com.nicha.eventticketing.data.remote.dto.auth.UserDto
import me.leolin.shortcutbadger.ShortcutBadger

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    @Inject
    lateinit var roleBasedNavigation: RoleBasedNavigation
    
    @Inject
    lateinit var permissionUtils: PermissionUtils
    
    @Inject
    lateinit var fcmTokenService: FcmTokenService
    
    // Lưu trữ thông tin điều hướng sâu
    private var deepLinkAction: String? = null
    private var deepLinkEventId: String? = null
    private var deepLinkTicketId: String? = null
    
    // Đăng ký launcher để xử lý kết quả yêu cầu quyền
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Timber.d("Tất cả quyền đã được cấp")
            if (preferencesManager.getAuthTokenSync() != null) {
                fcmTokenService.registerFcmToken()
            }
        } else {
            Timber.d("Một số quyền bị từ chối: ${permissions.filterValues { !it }}")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        checkAndRequestPermissions()
        
        handleIntent(intent)
        
        setContent {
            EventTicketingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val currentUser by authViewModel.currentUser.collectAsState()
                    val authState by authViewModel.authState.collectAsState()
                    
                    // Sử dụng trạng thái để theo dõi điểm đến đã được xác định chưa
                    var startDestination by rememberSaveable { 
                        mutableStateOf<String?>(null) 
                    }
                    
                    // Xác định điểm đến ban đầu dựa trên trạng thái xác thực
                    LaunchedEffect(Unit) {
                        val authToken = preferencesManager.getAuthTokenSync()
                        if (authToken == null) {
                            // Kiểm tra onboarding đã hoàn thành chưa
                            val isOnboardingCompleted = preferencesManager.isOnboardingCompletedSync()
                            startDestination = if (isOnboardingCompleted) {
                                Timber.d("Onboarding đã hoàn thành, chuyển đến màn hình đăng nhập")
                                NavDestination.Login.route
                            } else {
                                Timber.d("Chuyển đến màn hình splash và onboarding")
                                NavDestination.Splash.route
                            }
                        } else {
                            Timber.d("Token xác thực tồn tại, đăng ký FCM token")
                            fcmTokenService.registerFcmToken()
                        }
                    }
                    
                    // Khi thông tin người dùng đã được tải, cập nhật điểm đến
                    LaunchedEffect(currentUser) {
                        if (currentUser != null) {
                            var homeDestination = roleBasedNavigation.getHomeDestinationForUser(currentUser)
                            
                            if (deepLinkAction != null && currentUser != null) {
                                when (deepLinkAction) {
                                    AppConfig.Notification.ACTION_OPEN_EVENT -> {
                                        deepLinkEventId?.let { eventId ->
                                            homeDestination = NavDestination.EventDetail
                                            startDestination = NavDestination.EventDetail.createRoute(eventId)
                                            deepLinkEventId = null
                                        }
                                    }
                                    AppConfig.Notification.ACTION_OPEN_TICKET -> {
                                        deepLinkTicketId?.let { ticketId ->
                                            homeDestination = NavDestination.TicketDetail
                                            startDestination = NavDestination.TicketDetail.createRoute(ticketId)
                                            deepLinkTicketId = null
                                        }
                                    }
                                    AppConfig.Notification.ACTION_OPEN_COMMENT -> {
                                        deepLinkEventId?.let { eventId ->
                                            homeDestination = NavDestination.EventDetail
                                            startDestination = NavDestination.EventDetail.createRoute(eventId, true)
                                            deepLinkEventId = null
                                        }
                                    }
                                    AppConfig.Notification.ACTION_OPEN_NOTIFICATION -> {
                                        homeDestination = NavDestination.Notifications
                                        startDestination = NavDestination.Notifications.route
                                    }
                                }
                                deepLinkAction = null
                            }
                            
                            Timber.d("Người dùng đã được tải, vai trò: ${currentUser?.role}, điều hướng đến: ${homeDestination.route}")
                            startDestination = homeDestination.route
                        }
                    }
                    
                    LaunchedEffect(authState) {
                        Timber.d("MainActivity: AuthState thay đổi: $authState")
                        when (authState) {
                            is AuthState.Authenticated -> {
                                if (startDestination == null) {
                                    val user = currentUser
                                    if (user != null) {
                                        val homeDestination = roleBasedNavigation.getHomeDestinationForUser(user)
                                        Timber.d("Đã xác thực, vai trò: ${user.role}, điều hướng đến: ${homeDestination.route}")
                                        startDestination = homeDestination.route
                                        
                                        fcmTokenService.registerFcmToken()
                                        
                                        updateBadgeCount(0)
                                    }
                                }
                            }
                            is AuthState.Unauthenticated -> {
                                val isOnboardingCompleted = preferencesManager.isOnboardingCompletedSync()
                                val loginDestination = if (isOnboardingCompleted) {
                                    NavDestination.Login.route
                                } else {
                                    NavDestination.Splash.route
                                }
                                
                                if (startDestination != loginDestination && startDestination != null) {
                                    navController.navigate(loginDestination) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                                startDestination = loginDestination
                            }
                            else -> {
                            }
                        }
                    }
                    
                    // Hiển thị NavGraph chỉ khi đã xác định điểm đến
                    startDestination?.let { destination ->
                        NavGraph(
                            navController = navController,
                            startDestination = destination,
                            roleBasedNavigation = roleBasedNavigation,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    /**
     * Xử lý intent khi ứng dụng được mở từ thông báo
     */
    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        
        val action = intent.action
        
        when (action) {
            AppConfig.Notification.ACTION_OPEN_EVENT -> {
                deepLinkAction = action
                deepLinkEventId = intent.getStringExtra("eventId")
                Timber.d("Nhận deep link mở sự kiện: $deepLinkEventId")
            }
            AppConfig.Notification.ACTION_OPEN_TICKET -> {
                deepLinkAction = action
                deepLinkTicketId = intent.getStringExtra("ticketId")
                Timber.d("Nhận deep link mở vé: $deepLinkTicketId")
            }
            AppConfig.Notification.ACTION_OPEN_COMMENT -> {
                deepLinkAction = action
                deepLinkEventId = intent.getStringExtra("eventId")
                Timber.d("Nhận deep link mở bình luận của sự kiện: $deepLinkEventId")
            }
            AppConfig.Notification.ACTION_OPEN_NOTIFICATION -> {
                deepLinkAction = action
                Timber.d("Nhận deep link mở màn hình thông báo")
            }
        }
    }
    
    /**
     * Cập nhật badge count
     */
    private fun updateBadgeCount(count: Int) {
        try {
            ShortcutBadger.applyCount(applicationContext, count)
        } catch (e: Exception) {
            Timber.e(e, "Lỗi khi cập nhật badge count")
        }
    }
    
    /**
     * Kiểm tra và yêu cầu quyền cần thiết
     */
    private fun checkAndRequestPermissions() {
        val notGrantedPermissions = permissionUtils.getNotGrantedPermissions(this)
        if (notGrantedPermissions.isNotEmpty()) {
            Timber.d("Yêu cầu quyền: ${notGrantedPermissions.joinToString()}")
            requestPermissionLauncher.launch(notGrantedPermissions.toTypedArray())
        } else {
            Timber.d("Tất cả các quyền đã được cấp")
        }
    }
}