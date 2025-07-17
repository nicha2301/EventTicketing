package com.nicha.eventticketing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.nicha.eventticketing.data.preferences.PreferencesManager
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
import com.nicha.eventticketing.util.PermissionUtils
import com.nicha.eventticketing.data.remote.dto.auth.UserDto

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    @Inject
    lateinit var roleBasedNavigation: RoleBasedNavigation
    
    @Inject
    lateinit var permissionUtils: PermissionUtils
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
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
                    }
                        // Nếu có token, sẽ đợi AuthViewModel tải thông tin người dùng
                    }
                    
                    // Khi thông tin người dùng đã được tải, cập nhật điểm đến
                    LaunchedEffect(currentUser) {
                        if (currentUser != null) {
                            val homeDestination = roleBasedNavigation.getHomeDestinationForUser(currentUser)
                            Timber.d("Người dùng đã được tải, vai trò: ${currentUser?.role}, điều hướng đến: ${homeDestination.route}")
                            startDestination = homeDestination.route
                        }
                    }
                    
                    // Khi trạng thái xác thực thay đổi, cập nhật điểm đến
                    LaunchedEffect(authState) {
                        when (authState) {
                            is AuthState.Authenticated -> {
                                if (startDestination == null) {
                                    val user = currentUser
                                    if (user != null) {
                                        val homeDestination = roleBasedNavigation.getHomeDestinationForUser(user)
                                        Timber.d("Đã xác thực, vai trò: ${user.role}, điều hướng đến: ${homeDestination.route}")
                                        startDestination = homeDestination.route
                                    }
                                }
                            }
                            is AuthState.Unauthenticated -> {
                                val isOnboardingCompleted = preferencesManager.isOnboardingCompletedSync()
                                startDestination = if (isOnboardingCompleted) {
                                    NavDestination.Login.route
                                } else {
                                    NavDestination.Splash.route
                                }
                            }
                            else -> {
                                // Đang tải hoặc trạng thái khác, giữ nguyên điểm đến hiện tại
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
}