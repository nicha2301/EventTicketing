package com.nicha.eventticketing.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nicha.eventticketing.R
import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.EventCard
import com.nicha.eventticketing.ui.components.skeleton.CategoryItemSkeleton
import com.nicha.eventticketing.ui.components.skeleton.EventCardSkeleton
import com.nicha.eventticketing.ui.components.skeleton.EventListItemSkeleton
import com.nicha.eventticketing.viewmodel.CategoryViewModel
import com.nicha.eventticketing.viewmodel.EventViewModel
import kotlinx.coroutines.launch
import com.nicha.eventticketing.util.FormatUtils
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.graphicsLayer
import com.nicha.eventticketing.util.ImageUtils.getPrimaryImageUrl
import com.nicha.eventticketing.ui.components.NotificationIconWithBadge
import com.nicha.eventticketing.viewmodel.NotificationViewModel
import com.nicha.eventticketing.util.NetworkStatusObserver
import com.nicha.eventticketing.ui.theme.BrandOrange
import com.nicha.eventticketing.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onTicketsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onExploreClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onCategoryClick: (String) -> Unit = {},
    eventViewModel: EventViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Collect states from ViewModels
    val featuredEventsState by eventViewModel.featuredEventsState.collectAsState()
    val upcomingEventsState by eventViewModel.upcomingEventsState.collectAsState()
    val categoriesState by categoryViewModel.categoriesState.collectAsState()
    val categoryEventsState by eventViewModel.categoryEventsState.collectAsState()
    val allEventsState by eventViewModel.allEventsState.collectAsState()
    val selectedCategoryId by eventViewModel.selectedCategoryId.collectAsState()
    val unreadCountState by notificationViewModel.unreadCountState.collectAsState()
    
    val isFeaturedLoading = featuredEventsState is ResourceState.Loading
    val isUpcomingLoading = upcomingEventsState is ResourceState.Loading
    val isCategoriesLoading = categoriesState is ResourceState.Loading
    val isCategoryEventsLoading = categoryEventsState is ResourceState.Loading
    
    val featuredError = if (featuredEventsState is ResourceState.Error) 
                          (featuredEventsState as ResourceState.Error).message else null
    val upcomingError = if (upcomingEventsState is ResourceState.Error) 
                          (upcomingEventsState as ResourceState.Error).message else null
    val categoriesError = if (categoriesState is ResourceState.Error) 
                          (categoriesState as ResourceState.Error).message else null
    val categoryEventsError = if (categoryEventsState is ResourceState.Error) 
                          (categoryEventsState as ResourceState.Error).message else null
    
    val unreadCount = if (unreadCountState is ResourceState.Success) {
        (unreadCountState as ResourceState.Success).data.unreadCount
    } else 0
    val currentUser by authViewModel.currentUser.collectAsState()
    
    val context = LocalContext.current
    val isOnline by NetworkStatusObserver.observe(context).collectAsState(initial = true)
    
    LaunchedEffect(isOnline) { eventViewModel.setNetworkStatus(isOnline) }
    
    LaunchedEffect(isOnline) {
        eventViewModel.getFeaturedEvents()
        eventViewModel.getUpcomingEvents()
        eventViewModel.getAllEvents(page = 0, size = 200)
        categoryViewModel.getCategories()
        notificationViewModel.getUnreadNotificationCount()
    }
    
    val categories = when (categoriesState) {
        is ResourceState.Success -> {
            (categoriesState as ResourceState.Success<List<CategoryDto>>).data
        }
        else -> emptyList()
    }
    
    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        topBar = {},
        bottomBar = {
            HomeBottomNavBar(
                onHomeClick = {},
                onSearchClick = onSearchClick,
                onTicketsClick = onTicketsClick,
                onProfileClick = onProfileClick,
                onNotificationsClick = onNotificationsClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.Start
        ) {
            // Header: avatar + greeting (user name) + search icon
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    .height(96.dp)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                    AvatarWithBadge(
                        imageUrl = currentUser?.profilePictureUrl,
                        onClick = onProfileClick
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                            Text(
                        text = "Hi, " + (currentUser?.fullName?.substringBefore(" ")?.takeIf { it.isNotBlank() } ?: "Bạn"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E1E)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onSearchClick) {
                    Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF1E1E1E)
                        )
                    }
                }
            }
            val apiEvents: List<EventUi> = when {
                allEventsState is ResourceState.Success -> {
                    (allEventsState as ResourceState.Success<List<EventDto>>).data.map { e ->
                        val price = FormatUtils.formatEventPrice(e.minTicketPrice, e.isFree)
                        EventUi(
                            id = e.id,
                            title = e.title,
                            location = e.locationName,
                            priceText = price,
                            startDate = e.startDate
                        )
                    }
                }
                featuredEventsState is ResourceState.Success -> {
                    (featuredEventsState as ResourceState.Success<List<EventDto>>).data.map { e ->
                        val price = FormatUtils.formatEventPrice(e.minTicketPrice, e.isFree)
                        EventUi(
                            id = e.id,
                            title = e.title,
                            location = e.locationName,
                            priceText = price,
                            startDate = e.startDate
                        )
                    }
                }
                upcomingEventsState is ResourceState.Success -> {
                    (upcomingEventsState as ResourceState.Success<List<EventDto>>).data.map { e ->
                        val price = FormatUtils.formatEventPrice(e.minTicketPrice, e.isFree)
                        EventUi(
                            id = e.id,
                            title = e.title,
                            location = e.locationName,
                            priceText = price,
                            startDate = e.startDate
                        )
                    }
                }
                else -> emptyList()
            }
            val cardData = apiEvents
                        
                        Box(
                        modifier = Modifier
                            .fillMaxWidth() 
                    .padding(horizontal = 16.dp)
                    .offset(y = (-8).dp)
            ) {
                CardStack(
                    events = cardData,
                    onEventClick = onEventClick,
                                        modifier = Modifier
                                            .fillMaxWidth()
                        .height(620.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Swipe left or right to view more events",
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = Color.Black.copy(alpha = 0.2f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
                        
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

private data class EventUi(
    val id: String,
    val title: String,
    val location: String,
    val priceText: String,
    val startDate: String
)

@Composable
private fun CardStack(
    events: List<EventUi>,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxShown: Int = 3,
    swipeThreshold: Float = 120f 
) {
    var topIndex by remember { mutableStateOf(0) }
    val total = events.size
    var dragProgress by remember { mutableStateOf(0f) }

    val density = LocalDensity.current
    if (total == 0) {
        Box(modifier = modifier)
        return
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val shown = minOf(maxShown, total)
        for (i in (shown - 1) downTo 0) {
            val itemIndex = (topIndex + i) % total
            val event = events[itemIndex]

            val isTop = i == 0
            val scale = when (i) {
                0 -> 1f
                1 -> 0.96f + 0.04f * dragProgress
                else -> 0.92f
            }
            val translateY = when (i) {
                0 -> 0.dp
                1 -> 14.dp * (1 - dragProgress)
                else -> 26.dp
            }

            val initialAngle = when (i) { 1 -> -6f; 2 -> 6f; else -> 0f }
            val initialTranslateX = when (i) { 1 -> (-20).dp; 2 -> 20.dp; else -> 0.dp }
            val progressFactor = if (i == 1) (1 - dragProgress) else 1f
            val baseRotation = initialAngle * progressFactor
            val baseTranslateX = density.run { initialTranslateX.toPx() * progressFactor }

            SwipeableCard(
                event = event,
                isTop = isTop,
                scale = scale,
                translateY = translateY,
                onSwiped = { _ ->
                    topIndex = (topIndex + 1) % total
                    dragProgress = 0f
                },
                onDragProgress = { p -> dragProgress = p },
                baseTranslateX = baseTranslateX,
                baseRotation = baseRotation,
                onCardClick = onEventClick
            )
        }
    }
}

@Composable
private fun SwipeableCard(
    event: EventUi,
    isTop: Boolean,
    scale: Float,
    translateY: Dp,
    onSwiped: (direction: Int) -> Unit,
    onDragProgress: (Float) -> Unit,
    baseTranslateX: Float,
    baseRotation: Float,
    onCardClick: (String) -> Unit
) {
    // Animated offsets
    val animX = remember { Animatable(0f) }
    val animY = remember { Animatable(0f) }
    val rotation = (animX.value / 40f).coerceIn(-18f, 18f)
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp * LocalDensity.current.density
    val screenHeight = configuration.screenHeightDp * LocalDensity.current.density
    val thresholdPx = minOf(screenWidth, screenHeight) * 0.45f
    var isDragging by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(targetValue = if (isDragging && isTop) 24.dp else 8.dp, label = "card-elev")
                        
    Box(
            modifier = Modifier
            .fillMaxSize(0.95f)
            .zIndex(if (isTop) 3f else if (scale > 0.95f) 2f else 1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = translateY.toPx()
                translationX = if (isTop) animX.value else baseTranslateX
                this.rotationZ = if (isTop) rotation else baseRotation
            }
            .then(
                if (isTop) Modifier.pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch { animX.snapTo(animX.value + dragAmount.x) }
                            scope.launch { animY.snapTo(animY.value + dragAmount.y) }
                            val distance = kotlin.math.hypot(animX.value, animY.value)
                            onDragProgress((distance / thresholdPx).coerceIn(0f, 1f))
                        },
                        onDragEnd = {
                            isDragging = false
                            val distance = kotlin.math.hypot(animX.value, animY.value)
                            if (distance > thresholdPx) {
                                val dirX = if (distance == 0f) 0f else animX.value / distance
                                val dirY = if (distance == 0f) 0f else animY.value / distance
                                val targetX = animX.value + dirX * (screenWidth * 1.2f)
                                val targetY = animY.value + dirY * (screenHeight * 1.2f)
                                scope.launch {
                                    animX.animateTo(targetX, animationSpec = tween(260))
                                }
                                scope.launch {
                                    animY.animateTo(targetY, animationSpec = tween(260))
                                }.invokeOnCompletion {
                                    onSwiped(if (dirX >= 0) 1 else -1)
                                    scope.launch { animX.snapTo(0f) }
                                    scope.launch { animY.snapTo(0f) }
                                }
                            } else {
                                scope.launch {
                                    animX.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                                }
                                scope.launch {
                                    animY.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                                }
                                onDragProgress(0f)
                            }
                        }
                    )
                } else Modifier
            )
            .clickable(enabled = isTop && !isDragging, indication = null, interactionSource = remember { MutableInteractionSource() }) {
                onCardClick(event.id)
            }
    ) {
        HeroEventCard(
            title = event.title,
            location = event.location,
            priceText = event.priceText,
            startDate = event.startDate,
            elevation = elevation
        )
    }
}

@Composable
private fun HomeBottomNavBar(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onTicketsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Surface(shadowElevation = 6.dp) {
        Column {
            HorizontalDivider(color = Color(0xFFDADADA), thickness = 1.dp)
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = onHomeClick,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onSearchClick,
                    icon = { Icon(Icons.Default.DynamicFeed, contentDescription = "Feed") },
                    label = { Text("Feed") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onTicketsClick,
                    icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = "Tickets") },
                    label = { Text("Ticket") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onProfileClick,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    }
}

@Composable
private fun AvatarWithBadge(
    imageUrl: String?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
        .size(48.dp)
        .clip(CircleShape)
        .clickable(onClick = onClick)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar người dùng",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFFE7E7E7))
            )
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar mặc định",
                tint = Color(0xFF7A7A7A),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(28.dp)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(12.dp)
                .clip(CircleShape)
                .background(BrandOrange)
        )
    }
}

@Composable
private fun DateBadge(startDate: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (monthText, dayText) = remember(startDate) {
                try {
                    val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    val date = inputFormat.parse(startDate)
                    if (date != null) {
                        val month = java.text.SimpleDateFormat("MMM", java.util.Locale.ENGLISH).format(date).uppercase(java.util.Locale.ENGLISH)
                        val day = java.text.SimpleDateFormat("dd", java.util.Locale.getDefault()).format(date)
                        month to day
                    } else {
                        "" to ""
                    }
                } catch (e: Exception) {
                    "" to ""
                }
            }
            Text(text = monthText, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9DA3AF))
            Text(text = dayText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        }
    }
}

@Composable
private fun HeroEventCard(
    title: String,
    location: String,
    priceText: String,
    startDate: String,
    elevation: Dp = 6.dp
) {
    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0x1A000000))
                .offset(y = 18.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.95f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0x14000000))
                .offset(y = 9.dp)
        )

        Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxSize()
        ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val context = LocalContext.current
            val imageResId = com.nicha.eventticketing.R.drawable.rectangle_4168
            if (imageResId != 0) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF545454), Color(0xFFBDBDBD))
                            )
                        )
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .height(200.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.White.copy(alpha = 0.0f), 
                                0.5f to Color.White.copy(alpha = 1.0f), 
                                1.0f to Color.White.copy(alpha = 1.0f)  
                            )
                        )
                    )
            )

            Box(modifier = Modifier.padding(16.dp)) { DateBadge(startDate) }
                    
                            Column(
                    modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111111)
                )
                                    Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = BrandOrange) {
                        Text(
                            text = priceText,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF9DA3AF), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = location, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                }
            }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: CategoryDto,
    isSelected: Boolean,
    onClick: () -> Unit,
    fallbackIcon: ImageVector = Icons.Filled.Category
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    
    // Hiệu ứng scale khi được chọn
    val scale = if (isSelected) 1.05f else 1f
    
    // Màu gradient cho nền khi được chọn
    val backgroundBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface
            )
        )
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .padding(vertical = 4.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Card(
            modifier = Modifier
                .size(64.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            shape = CircleShape,
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 6.dp else 1.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = if (isSelected) 0.dp else 0.5.dp,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush),
                contentAlignment = Alignment.Center
            ) {
                if (category.iconUrl != null) {
                    // Sử dụng iconUrl từ API
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(6.dp)
                            .background(
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(category.iconUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = category.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(4.dp),
                            error = painterResource(id = R.drawable.ic_category_default)
                        )
                    }
                } else {
                    // Sử dụng biểu tượng fallback nếu không có iconUrl
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(6.dp)
                            .background(
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = fallbackIcon,
                            contentDescription = category.name,
                            tint = if (isSelected) 
                                MaterialTheme.colorScheme.onPrimary
                            else 
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(24.dp)
                        )
                    }
                }
            }
        }
                
        Spacer(modifier = Modifier.height(8.dp))
                
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) 
                MaterialTheme.colorScheme.primary
            else 
                MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
} 

@Composable
fun EventListItem(
    event: EventDto,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    val interactionSource = remember { MutableInteractionSource() }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(cardShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event image with overlay gradient
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(event.getPrimaryImageUrl())
                        .crossfade(true)
                        .build(),
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.1f),
                                    Color.Black.copy(alpha = 0.3f)
                                ),
                                startY = 0f,
                                endY = 300f
                            )
                        )
                )
                
                // Status indicator
                if (event.isFeatured || event.isFree) {
                    Surface(
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        color = when {
                            event.isFeatured -> MaterialTheme.colorScheme.tertiary
                            event.isFree -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = when {
                                event.isFeatured -> "Nổi bật"
                                event.isFree -> "Miễn phí"
                                else -> ""
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                event.isFeatured -> MaterialTheme.colorScheme.onTertiary
                                event.isFree -> MaterialTheme.colorScheme.onSecondary
                                else -> MaterialTheme.colorScheme.onPrimary
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Event details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                // Title
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date and location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(3.dp)
                                .size(14.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = FormatUtils.formatDate(event.startDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(3.dp)
                                .size(14.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        text = event.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Price tag
                val priceText = FormatUtils.formatEventPrice(event.minTicketPrice, event.isFree)
                
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
} 