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
import com.nicha.eventticketing.ui.theme.EventTicketingTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
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
                    
                    // Determine start destination based on authentication state
                    val authToken = preferencesManager.getAuthTokenSync()
                    val startDestination = if (authToken != null) {
                        Timber.d("Người dùng đã đăng nhập, chuyển đến màn hình chính")
                        NavDestination.Home.route
                    } else {
                        // Check if onboarding is completed
                        val isOnboardingCompleted by preferencesManager.isOnboardingCompleted()
                            .collectAsState(initial = false)
                        
                        if (isOnboardingCompleted) {
                            Timber.d("Onboarding đã hoàn thành, chuyển đến màn hình đăng nhập")
                            NavDestination.Login.route
                        } else {
                            Timber.d("Chuyển đến màn hình splash và onboarding")
                            NavDestination.Splash.route
                        }
                    }
                    
                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}