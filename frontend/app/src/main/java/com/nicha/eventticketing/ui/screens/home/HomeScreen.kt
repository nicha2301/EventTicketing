package com.nicha.eventticketing.ui.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nicha.eventticketing.R
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.theme.BrandOrange
import com.nicha.eventticketing.util.FormatUtils
import com.nicha.eventticketing.util.NetworkStatusObserver
import com.nicha.eventticketing.viewmodel.AuthViewModel
import com.nicha.eventticketing.viewmodel.CategoryViewModel
import com.nicha.eventticketing.viewmodel.EventViewModel
import com.nicha.eventticketing.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onTicketsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit = {},
    eventViewModel: EventViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val featuredEventsState by eventViewModel.featuredEventsState.collectAsState()
    val upcomingEventsState by eventViewModel.upcomingEventsState.collectAsState()
    val allEventsState by eventViewModel.allEventsState.collectAsState()
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

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        topBar = {},
        bottomBar = {
            HomeBottomNavBar(
                onHomeClick = {},
                onNotificationClick = onNotificationClick,
                onTicketsClick = onTicketsClick,
                onProfileClick = onProfileClick,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.Start
        ) {
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
                        text = "Hi, " + (currentUser?.fullName?.substringBefore(" ")
                            ?.takeIf { it.isNotBlank() } ?: "Bạn"),
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
                        val img = e.imageUrls.firstOrNull()
                        EventUi(
                            id = e.id,
                            title = e.title,
                            location = e.locationName,
                            priceText = price,
                            startDate = e.startDate,
                            imageUrl = img
                        )
                    }
                }

                featuredEventsState is ResourceState.Success -> {
                    (featuredEventsState as ResourceState.Success<List<EventDto>>).data.map { e ->
                        val price = FormatUtils.formatEventPrice(e.minTicketPrice, e.isFree)
                        val img = e.featuredImageUrl?.takeIf { it.isNotBlank() }
                            ?: e.imageUrls.firstOrNull()
                        EventUi(
                            id = e.id,
                            title = e.title,
                            location = e.locationName,
                            priceText = price,
                            startDate = e.startDate,
                            imageUrl = img
                        )
                    }
                }

                upcomingEventsState is ResourceState.Success -> {
                    (upcomingEventsState as ResourceState.Success<List<EventDto>>).data.map { e ->
                        val price = FormatUtils.formatEventPrice(e.minTicketPrice, e.isFree)
                        val img = e.featuredImageUrl?.takeIf { it.isNotBlank() }
                            ?: e.imageUrls.firstOrNull()
                        EventUi(
                            id = e.id,
                            title = e.title,
                            location = e.locationName,
                            priceText = price,
                            startDate = e.startDate,
                            imageUrl = img
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
    val startDate: String,
    val imageUrl: String?
)

@Composable
private fun CardStack(
    events: List<EventUi>,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxShown: Int = 3,
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

            val initialAngle = when (i) {
                1 -> -6f; 2 -> 6f; else -> 0f
            }
            val initialTranslateX = when (i) {
                1 -> (-20).dp; 2 -> 20.dp; else -> 0.dp
            }
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
    val elevation by animateDpAsState(
        targetValue = if (isDragging && isTop) 8.dp else 4.dp,
        label = "card-elev"
    )

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
                                    animX.animateTo(
                                        0f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                    )
                                }
                                scope.launch {
                                    animY.animateTo(
                                        0f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                    )
                                }
                                onDragProgress(0f)
                            }
                        }
                    )
                } else Modifier
            )
            .clickable(
                enabled = isTop && !isDragging,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                onCardClick(event.id)
            }
    ) {
        HeroEventCard(
            title = event.title,
            location = event.location,
            priceText = event.priceText,
            startDate = event.startDate,
            imageUrl = event.imageUrl,
            elevation = elevation
        )
    }
}

@Composable
private fun HomeBottomNavBar(
    onHomeClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onTicketsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = Color(0xFFFFFFFF),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E5E5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home (Active)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = "Home",
                    tint = Color(0xFF171924),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sự kiện",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF171924)
                )
            }

            // Notification
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 28.dp)
                    ) { onNotificationClick() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bell),
                    contentDescription = "Notification",
                    tint = Color(0xFFA9A9A9),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Thông báo",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFA9A9A9)
                )
            }

            // Ticket
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 28.dp)
                    ) { onTicketsClick() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ticket),
                    contentDescription = "Ticket",
                    tint = Color(0xFFA9A9A9),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Vé",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFA9A9A9)
                )
            }

            // Profile
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 28.dp)
                    ) { onProfileClick() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = "Profile",
                    tint = Color(0xFFA9A9A9),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tài khoản",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFA9A9A9)
                )
            }
        }
        Spacer(
            modifier = Modifier.height(
                with(LocalDensity.current) {
                    WindowInsets.navigationBars.getBottom(this).toDp() + 70.dp
                }
            )
        )
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
                    val inputFormat = java.text.SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        java.util.Locale.getDefault()
                    )
                    val date = inputFormat.parse(startDate)
                    if (date != null) {
                        val month =
                            java.text.SimpleDateFormat("MMM", java.util.Locale.ENGLISH).format(date)
                                .uppercase(java.util.Locale.ENGLISH)
                        val day = java.text.SimpleDateFormat("dd", java.util.Locale.getDefault())
                            .format(date)
                        month to day
                    } else {
                        "" to ""
                    }
                } catch (e: Exception) {
                    "" to ""
                }
            }
            Text(
                text = monthText,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9DA3AF)
            )
            Text(
                text = dayText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        }
    }
}

@Composable
private fun HeroEventCard(
    title: String,
    location: String,
    priceText: String,
    startDate: String,
    imageUrl: String?,
    elevation: Dp = 4.dp
) {
    Box(modifier = Modifier.padding(horizontal = 8.dp)) {
        // Hiệu ứng thẻ chồng thẻ: 2 lớp mờ phía sau (tăng shadow bottom và góc)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0x20000000))
                .offset(y = 20.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.95f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0x15000000))
                .offset(y = 10.dp)
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation,
                pressedElevation = elevation,
                focusedElevation = elevation,
                hoveredElevation = elevation
            ),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(id = R.drawable.image_placeholder),
                        error = painterResource(id = R.drawable.ic_broken_image),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback gradient khi không có ảnh
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
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF9DA3AF),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
} 