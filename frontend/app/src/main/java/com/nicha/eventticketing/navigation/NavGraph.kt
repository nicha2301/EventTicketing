package com.nicha.eventticketing.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nicha.eventticketing.ui.screens.auth.ForgotPasswordScreen
import com.nicha.eventticketing.ui.screens.auth.LoginScreen
import com.nicha.eventticketing.ui.screens.auth.RegisterScreen
import com.nicha.eventticketing.ui.screens.auth.ResetPasswordScreen
import com.nicha.eventticketing.ui.screens.checkin.CheckInScreen
import com.nicha.eventticketing.ui.screens.analytics.AnalyticsDashboardScreen
import com.nicha.eventticketing.ui.screens.analytics.RevenueAnalyticsScreen
import com.nicha.eventticketing.ui.screens.analytics.TicketAnalyticsScreen
import com.nicha.eventticketing.ui.screens.analytics.AttendeeAnalyticsScreen
import com.nicha.eventticketing.ui.screens.analytics.EventPerformanceScreen
import com.nicha.eventticketing.ui.screens.demo.NeumorphicDemoScreen
import com.nicha.eventticketing.ui.screens.event.EventDetailScreen
import com.nicha.eventticketing.ui.screens.home.HomeScreen
import com.nicha.eventticketing.ui.screens.onboarding.OnboardingScreen
import com.nicha.eventticketing.ui.screens.organizer.EventDashboardScreen
import com.nicha.eventticketing.ui.screens.organizer.OrganizerEventListScreen
import com.nicha.eventticketing.ui.screens.organizer.OrganizerEventDetailScreen
import com.nicha.eventticketing.ui.screens.organizer.EventImagesScreen
import com.nicha.eventticketing.ui.screens.organizer.TicketTypeListScreen
import com.nicha.eventticketing.ui.screens.payment.PaymentScreen
import com.nicha.eventticketing.ui.screens.profile.ProfileScreen
import com.nicha.eventticketing.ui.screens.search.SearchScreen
import com.nicha.eventticketing.ui.screens.splash.SplashScreen
import com.nicha.eventticketing.ui.screens.tickets.TicketDetailScreen
import com.nicha.eventticketing.ui.screens.tickets.TicketWalletScreen
import com.nicha.eventticketing.ui.screens.profile.EditProfileScreen
import com.nicha.eventticketing.ui.screens.auth.UnauthorizedScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.nicha.eventticketing.viewmodel.AuthState
import com.nicha.eventticketing.viewmodel.AuthViewModel
import com.nicha.eventticketing.ui.screens.organizer.EditEventScreen
import com.nicha.eventticketing.ui.screens.organizer.CreateEventScreen
import com.nicha.eventticketing.ui.screens.organizer.OrganizerProfileScreen
import com.nicha.eventticketing.ui.screens.notification.NotificationsScreen
import com.nicha.eventticketing.ui.screens.organizer.TicketTypeListScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = NavDestination.Splash.route,
    roleBasedNavigation: RoleBasedNavigation,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(route = NavDestination.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(NavDestination.Onboarding.route) {
                        popUpTo(NavDestination.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Onboarding Screen
        composable(route = NavDestination.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(NavDestination.Login.route) {
                        popUpTo(NavDestination.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Login Screen
        composable(route = NavDestination.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    currentUser?.let { user ->
                        val homeDestination = roleBasedNavigation.getHomeDestinationForUser(user)
                        navController.navigate(homeDestination.route) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    } ?: run {
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(NavDestination.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(NavDestination.ForgotPassword.route)
                }
            )
        }
        
        // Register Screen
        composable(route = NavDestination.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(NavDestination.Login.route) {
                        popUpTo(NavDestination.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(NavDestination.Login.route) {
                        popUpTo(NavDestination.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Forgot Password Screen
        composable(route = NavDestination.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onResetLinkSent = {
                    // Không chuyển màn hình ngay, chỉ hiển thị thông báo thành công
                }
            )
        }
        
        // Reset Password Screen
        composable(
            route = NavDestination.ResetPassword.route + "/{token}",
            arguments = listOf(
                navArgument("token") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            ResetPasswordScreen(
                token = token,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onResetSuccess = {
                    navController.navigate(NavDestination.Login.route) {
                        popUpTo(NavDestination.ResetPassword.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Home Screen
        composable(route = NavDestination.Home.route) {
            HomeScreen(
                onEventClick = { eventId ->
                    navController.navigate(NavDestination.EventDetail.createRoute(eventId))
                },
                onSearchClick = {
                    navController.navigate(NavDestination.Search.route)
                },
                onTicketsClick = {
                    navController.navigate(NavDestination.TicketWallet.route)
                },
                onProfileClick = {
                    navController.navigate(NavDestination.Profile.route)
                },
                onExploreClick = {
                    // Implement explore navigation if needed
                },
                onNotificationsClick = {
                    navController.navigate(NavDestination.Notifications.route)
                }
            )
        }
        
        // Event Detail Screen
        composable(
            route = NavDestination.EventDetail.route + "/{eventId}",
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(
                eventId = eventId,
                onBackClick = {
                    navController.popBackStack()
                },
                onBuyTicketsClick = { eventId, ticketTypeId, existingTicketId ->
                    if (existingTicketId != null) {
                        navController.navigate(NavDestination.Payment.createRoute(eventId, ticketTypeId, 1, existingTicketId))
                    } else {
                        navController.navigate(NavDestination.Payment.createRoute(eventId, ticketTypeId, 1))
                    }
                },
                onViewTicketClick = { ticketId ->
                    navController.navigate(NavDestination.TicketDetail.createRoute(ticketId))
                }
            )
        }
        
        // Search Screen
        composable(route = NavDestination.Search.route) {
            SearchScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onEventClick = { eventId ->
                    navController.navigate(NavDestination.EventDetail.createRoute(eventId))
                }
            )
        }
        
        // Profile Screen
        composable(route = NavDestination.Profile.route) {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onEditClick = {
                    navController.navigate(NavDestination.EditProfile.route)
                },
                onSettingsClick = {
                    // Navigate to settings if needed
                },
                onNotificationsClick = {
                    navController.navigate(NavDestination.Notifications.route)
                },
                onSecurityClick = {
                    navController.navigate(NavDestination.Security.route)
                },
                onPrivacyClick = {
                    navController.navigate(NavDestination.Privacy.route)
                },
                onLogoutClick = {
                },
                authViewModel = authViewModel
            )
        }
        
        // Organizer Profile Screen
        composable(route = NavDestination.OrganizerProfile.route) {
            // Kiểm tra quyền truy cập
            LaunchedEffect(currentUser) {
                if (!roleBasedNavigation.canAccessDestination(NavDestination.OrganizerProfile, currentUser)) {
                    navController.navigate(NavDestination.Unauthorized.route)
                }
            }
            
            OrganizerProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onEditClick = {
                    navController.navigate("edit_profile")
                },
                onSettingsClick = {
                    navController.navigate("notifications")
                },
                onMyEventsClick = {
                    navController.navigate(NavDestination.EventDashboard.route)
                },
                onCreateEventClick = {
                    navController.navigate(NavDestination.CreateEvent.route)
                },
                onEventDashboardClick = {
                    navController.navigate(NavDestination.EventDashboard.route)
                },
                onAnalyticsClick = {
                    navController.navigate(NavDestination.AnalyticsDashboard.route)
                },
                onScanQRClick = {
                    navController.navigate(NavDestination.CheckIn.route)
                },
                onLogoutClick = {
                },
                authViewModel = authViewModel
            )
        }
        
        // Notifications Screen
        composable(route = NavDestination.Notifications.route) {
            NotificationsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onNotificationClick = { referenceType, referenceId, notificationType ->
                    when (referenceType) {
                        "EVENT" -> {
                            if (referenceId.isNotBlank()) {
                                navController.navigate(NavDestination.EventDetail.createRoute(referenceId))
                            }
                        }
                        "TICKET" -> {
                            if (referenceId.isNotBlank()) {
                                navController.navigate(NavDestination.TicketDetail.createRoute(referenceId))
                            }
                        }
                    }
                }
            )
        }
        
        // Security Screen
        composable(route = "security") {
            com.nicha.eventticketing.ui.screens.settings.SecurityScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Privacy Screen
        composable(route = "privacy") {
            com.nicha.eventticketing.ui.screens.settings.PrivacyScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Edit Profile Screen
        composable(route = "edit_profile") {
            EditProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Ticket Wallet Screen
        composable(route = NavDestination.TicketWallet.route) {
            TicketWalletScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onTicketClick = { ticketId ->
                    navController.navigate(NavDestination.TicketDetail.createRoute(ticketId))
                }
            )
        }
        
        // Ticket Detail Screen
        composable(
            route = NavDestination.TicketDetail.route + "/{ticketId}",
            arguments = listOf(
                navArgument("ticketId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            TicketDetailScreen(
                ticketId = ticketId,
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToPayment = { eventId, ticketTypeId, quantity, existingTicketId ->
                    val route = if (existingTicketId != null) {
                        "${NavDestination.Payment.route}/$eventId/$ticketTypeId/$quantity?existingTicketId=$existingTicketId"
                    } else {
                        "${NavDestination.Payment.route}/$eventId/$ticketTypeId/$quantity"
                    }
                    navController.navigate(route)
                }
            )
        }
        
        // Payment Screen
        composable(
            route = NavDestination.Payment.route + "/{eventId}/{ticketType}/{quantity}?existingTicketId={existingTicketId}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("ticketType") { type = NavType.StringType },
                navArgument("quantity") { type = NavType.IntType },
                navArgument("existingTicketId") { 
                    type = NavType.StringType 
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val ticketType = backStackEntry.arguments?.getString("ticketType") ?: ""
            val quantity = backStackEntry.arguments?.getInt("quantity") ?: 1
            val existingTicketId = backStackEntry.arguments?.getString("existingTicketId")
            
            PaymentScreen(
                eventId = eventId,
                ticketTypeId = ticketType,
                quantity = quantity,
                existingTicketId = existingTicketId,
                onBackClick = {
                    navController.popBackStack()
                },
                onPaymentSuccess = {
                    navController.navigate(NavDestination.TicketWallet.route) {
                        popUpTo(NavDestination.Home.route) { 
                            inclusive = false 
                        }
                    }
                }
            )
        }
        
        // Check-in Screen
        composable(route = NavDestination.CheckIn.route) {
            CheckInScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Màn hình Unauthorized (thêm mới)
        composable(route = NavDestination.Unauthorized.route) {
            UnauthorizedScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.navigate(NavDestination.Login.route) {
                        popUpTo(NavDestination.Unauthorized.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Organizer Dashboard Screen
        composable(route = NavDestination.EventDashboard.route) {
            // Kiểm tra quyền truy cập
            LaunchedEffect(currentUser) {
                if (!roleBasedNavigation.canAccessDestination(NavDestination.EventDashboard, currentUser)) {
                    navController.navigate(NavDestination.Unauthorized.route)
                }
            }
            
            // Không hiển thị back button vì đây là trang home của oranizer
            EventDashboardScreen(
                onBackClick = {
                },
                showBackButton = false,
                onCreateEventClick = {
                    navController.navigate(NavDestination.CreateEvent.route)
                },
                onEventClick = { eventId ->
                    if (eventId == "profile") {
                        navController.navigate(NavDestination.OrganizerProfile.route)
                    } else
                    if (eventId == "list") {
                        navController.navigate(NavDestination.EventDashboard.route)
                    } else {
                        navController.navigate(NavDestination.OrganizerEventDetail.createRoute(eventId))
                    }
                },
                onScanQRClick = {
                    navController.navigate(NavDestination.CheckIn.route)
                }
            )
        }
        
        // Organizer Event List Screen
        composable(route = NavDestination.OrganizerEventList.route) {
            // Kiểm tra quyền truy cập
            LaunchedEffect(currentUser) {
                if (!roleBasedNavigation.canAccessDestination(NavDestination.OrganizerEventList, currentUser)) {
                    navController.navigate(NavDestination.Unauthorized.route)
                }
            }
            
            OrganizerEventListScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onEventClick = { eventId ->
                    navController.navigate(NavDestination.OrganizerEventDetail.createRoute(eventId))
                },
                onCreateEventClick = {
                    navController.navigate(NavDestination.CreateEvent.route)
                },
                onScanQRClick = {
                    navController.navigate(NavDestination.CheckIn.route)
                }
            )
        }
        
        // Organizer Event Detail Screen
        composable(
            route = NavDestination.OrganizerEventDetail.route + "/{eventId}",
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            
            // Kiểm tra quyền truy cập
            LaunchedEffect(currentUser) {
                if (!roleBasedNavigation.canAccessDestination(NavDestination.OrganizerEventDetail, currentUser)) {
                    navController.navigate(NavDestination.Unauthorized.route)
                }
            }
            
            OrganizerEventDetailScreen(
                eventId = eventId,
                onBackClick = {
                    navController.popBackStack()
                },
                onEditClick = { eventIdToEdit ->
                    // Thay thế bằng màn hình chỉnh sửa sự kiện khi có
                    navController.navigate(NavDestination.EditEvent.createRoute(eventIdToEdit))
                },
                onManageTicketsClick = { eventIdForTickets ->
                    navController.navigate(NavDestination.EventTicketTypes.createRoute(eventIdForTickets))
                },
                onManageImagesClick = { eventIdForImages ->
                    navController.navigate(NavDestination.EventImages.createRoute(eventIdForImages))
                },
                onAnalyticsClick = { eventIdForAnalytics ->
                    navController.navigate(NavDestination.AnalyticsDashboard.createRoute(eventIdForAnalytics))
                }
            )
        }
        
        // Event Images Screen
        composable(
            route = NavDestination.EventImages.route + "/{eventId}",
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            
            // Kiểm tra quyền truy cập
            LaunchedEffect(currentUser) {
                if (!roleBasedNavigation.canAccessDestination(NavDestination.EventImages, currentUser)) {
                    navController.navigate(NavDestination.Unauthorized.route)
                }
            }
            
            EventImagesScreen(
                eventId = eventId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Ticket Type List Screen
        composable(
            route = NavDestination.EventTicketTypes.route + "/{eventId}",
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            
            // Kiểm tra quyền truy cập
            LaunchedEffect(currentUser) {
                if (!roleBasedNavigation.canAccessDestination(NavDestination.EventTicketTypes, currentUser)) {
                    navController.navigate(NavDestination.Unauthorized.route)
                }
            }
            
            TicketTypeListScreen(
                eventId = eventId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Analytics Dashboard Screen
        composable(
            route = NavDestination.AnalyticsDashboard.route + "/{eventId}",
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            
            // Kiểm tra quyền truy cập
            LaunchedEffect(currentUser) {
                if (!roleBasedNavigation.canAccessDestination(NavDestination.AnalyticsDashboard, currentUser)) {
                    navController.navigate(NavDestination.Unauthorized.route)
                }
            }
            
            AnalyticsDashboardScreen(
                eventId = eventId,
                onBackClick = {
                    navController.popBackStack()
                },
                onNavigateToDetailed = { analyticsType ->
                    when (analyticsType) {
                        "revenue" -> {
                            if (eventId != null) {
                                navController.navigate(NavDestination.RevenueAnalytics.createRoute(eventId))
                            }
                        }
                        "ticket_sales" -> {
                            if (eventId != null) {
                                navController.navigate(NavDestination.TicketAnalytics.createRoute(eventId))
                            }
                        }
                        "attendee" -> {
                            if (eventId != null) {
                                navController.navigate(NavDestination.AttendeeAnalytics.createRoute(eventId))
                            }
                        }
                        "performance" -> {
                            if (eventId != null) {
                                navController.navigate(NavDestination.EventPerformance.createRoute(eventId))
                            }
                        }
                        else -> {
                            navController.navigate(NavDestination.EventDashboard.route)
                        }
                    }
                }
            )
        }
        
        // Revenue Analytics Screen
        composable(
            route = NavDestination.RevenueAnalytics.route + "/{eventId}",
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            
            RevenueAnalyticsScreen(
                eventId = eventId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Ticket Analytics Screen
        composable(
            route = NavDestination.TicketAnalytics.route + "/{eventId}",
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            
            TicketAnalyticsScreen(
                eventId = eventId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Attendee Analytics Screen
        composable(
            route = NavDestination.AttendeeAnalytics.route + "/{eventId}",
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            
            AttendeeAnalyticsScreen(
                eventId = eventId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Event Performance Screen
        composable(
            route = NavDestination.EventPerformance.route + "/{eventId}",
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            
            EventPerformanceScreen(
                eventId = eventId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Create Event Screen
        composable(route = NavDestination.CreateEvent.route) {
            CreateEventScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onEventCreated = { eventId ->
                    navController.navigate(NavDestination.EventDetail.createRoute(eventId)) {
                        popUpTo(NavDestination.CreateEvent.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Edit Event Screen
        composable(
            route = NavDestination.EditEvent.route + "/{eventId}",
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            
            // Kiểm tra quyền truy cập
            LaunchedEffect(currentUser) {
                if (!roleBasedNavigation.canAccessDestination(NavDestination.OrganizerEventDetail, currentUser)) {
                    navController.navigate(NavDestination.Unauthorized.route)
                }
            }
            
            EditEventScreen(
                eventId = eventId,
                onBackClick = {
                    navController.popBackStack()
                },
                onEventUpdated = { updatedEventId ->
                    navController.navigate(NavDestination.OrganizerEventDetail.createRoute(updatedEventId)) {
                        popUpTo(NavDestination.EditEvent.route + "/{eventId}") { inclusive = true }
                    }
                }
            )
        }
        
        // Neumorphic Demo Screen
        composable(route = "neumorphic_demo") {
            NeumorphicDemoScreen(
                onBackClick = {
                    navController.navigate(NavDestination.Home.route)
                }
            )
        }
    }
}

sealed class NavDestination(val route: String) {
    object Home : NavDestination("home")
    object Login : NavDestination("login")
    object Register : NavDestination("register")
    object ForgotPassword : NavDestination("forgot_password")
    object ResetPassword : NavDestination("reset_password") {
        fun createRoute(token: String) = "$route/$token"
    }
    object Splash : NavDestination("splash")
    object EventDetail : NavDestination("event_detail") {
        fun createRoute(eventId: String, openComments: Boolean = false): String {
            return if (openComments) {
                "$route/$eventId?openComments=true"
            } else {
                "$route/$eventId"
            }
        }
    }
    object TicketWallet : NavDestination("ticket_wallet")
    object TicketDetail : NavDestination("ticket_detail") {
        fun createRoute(ticketId: String) = "$route/$ticketId"
    }
    object Search : NavDestination("search")
    object Profile : NavDestination("profile")
    object EditProfile : NavDestination("edit_profile")
    object Payment : NavDestination("payment") {
        fun createRoute(eventId: String, ticketTypeId: String, quantity: Int) = 
            "$route/$eventId/$ticketTypeId/$quantity"
        fun createRoute(eventId: String, ticketTypeId: String, quantity: Int, existingTicketId: String?) = 
            if (existingTicketId != null) {
                "$route/$eventId/$ticketTypeId/$quantity?existingTicketId=$existingTicketId"
            } else {
                "$route/$eventId/$ticketTypeId/$quantity"
            }
    }
    object PaymentResult : NavDestination("payment_result") {
        fun createRoute(status: String, paymentId: String) = 
            "$route/$status/$paymentId"
    }
    object CreateEvent : NavDestination("create_event")
    object EditEvent : NavDestination("edit_event") {
        fun createRoute(eventId: String) = "$route/$eventId"
    }
    object EventDashboard : NavDestination("organizer_dashboard")
    object OrganizerEventList : NavDestination("organizer_event_list")
    object OrganizerEventDetail : NavDestination("organizer_event_detail") {
        fun createRoute(eventId: String) = "$route/$eventId"
    }
    object EventTicketTypes : NavDestination("event_ticket_types") {
        fun createRoute(eventId: String) = "$route/$eventId"
    }
    object EventImages : NavDestination("event_images") {
        fun createRoute(eventId: String) = "$route/$eventId"
    }
    object OrganizerProfile : NavDestination("organizer_profile")
    object CheckIn : NavDestination("check_in")
    object Onboarding : NavDestination("onboarding")
    object Unauthorized : NavDestination("unauthorized")
    object Notifications : NavDestination("notifications")
    object Security : NavDestination("security")
    object Privacy : NavDestination("privacy")
    object AnalyticsDashboard : NavDestination("analytics_dashboard") {
        fun createRoute(eventId: String) = "$route/$eventId"
    }
    object RevenueAnalytics : NavDestination("revenue_analytics") {
        fun createRoute(eventId: String) = "$route/$eventId"
    }
    object TicketAnalytics : NavDestination("ticket_analytics") {
        fun createRoute(eventId: String) = "$route/$eventId"
    }
    object AttendeeAnalytics : NavDestination("attendee_analytics") {
        fun createRoute(eventId: String) = "$route/$eventId"
    }
    object EventPerformance : NavDestination("event_performance") {
        fun createRoute(eventId: String) = "$route/$eventId"
    }
} 