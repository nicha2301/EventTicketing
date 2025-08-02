package com.nicha.eventticketing.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nicha.eventticketing.R
import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.EventCard
import com.nicha.eventticketing.ui.components.EventCarousel
import com.nicha.eventticketing.ui.components.skeleton.CategoryItemSkeleton
import com.nicha.eventticketing.ui.components.skeleton.EventCardSkeleton
import com.nicha.eventticketing.ui.components.skeleton.EventListItemSkeleton
import com.nicha.eventticketing.viewmodel.CategoryViewModel
import com.nicha.eventticketing.viewmodel.EventViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import androidx.compose.ui.text.style.TextAlign
import java.text.SimpleDateFormat
import java.util.Locale
import com.nicha.eventticketing.util.FormatUtils
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.graphicsLayer
import com.nicha.eventticketing.util.ImageUtils.getPrimaryImageUrl
import com.nicha.eventticketing.ui.components.NotificationIconWithBadge
import com.nicha.eventticketing.viewmodel.NotificationViewModel
import com.nicha.eventticketing.util.NetworkStatusObserver


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
    notificationViewModel: NotificationViewModel = hiltViewModel()
) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Collect states from ViewModels
    val featuredEventsState by eventViewModel.featuredEventsState.collectAsState()
    val upcomingEventsState by eventViewModel.upcomingEventsState.collectAsState()
    val categoriesState by categoryViewModel.categoriesState.collectAsState()
    val categoryEventsState by eventViewModel.categoryEventsState.collectAsState()
    val selectedCategoryId by eventViewModel.selectedCategoryId.collectAsState()
    val unreadCountState by notificationViewModel.unreadCountState.collectAsState()
    
    // Theo dõi trạng thái loading cho từng phần riêng biệt
    val isFeaturedLoading = featuredEventsState is ResourceState.Loading
    val isUpcomingLoading = upcomingEventsState is ResourceState.Loading
    val isCategoriesLoading = categoriesState is ResourceState.Loading
    val isCategoryEventsLoading = categoryEventsState is ResourceState.Loading
    
    // Theo dõi trạng thái lỗi cho từng phần riêng biệt
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
    } else {
        0
    }
    
    val context = LocalContext.current
    val isOnline by NetworkStatusObserver.observe(context).collectAsState(initial = true)
    
    LaunchedEffect(isOnline) {
        eventViewModel.setNetworkStatus(isOnline)
    }
    
    LaunchedEffect(isOnline) {
        eventViewModel.getFeaturedEvents()
        eventViewModel.getUpcomingEvents()
        categoryViewModel.getCategories()
        notificationViewModel.getUnreadNotificationCount()
    }
    
    
    // Lấy danh sách danh mục trực tiếp từ API
    val categories = when (categoriesState) {
        is ResourceState.Success -> {
            (categoriesState as ResourceState.Success<List<CategoryDto>>).data
        }
        else -> emptyList()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "EventTicketing",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    NotificationIconWithBadge(
                        count = unreadCount,
                        onClick = onNotificationsClick
                    )
                    IconButton(onClick = onTicketsClick) {
                        Icon(
                            imageVector = Icons.Filled.ConfirmationNumber,
                            contentDescription = "Vé của tôi"
                        )
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Hồ sơ"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isOnline) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Đang xem dữ liệu ngoại tuyến",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            
            // Search bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable(onClick = onSearchClick),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Tìm kiếm sự kiện...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Categories
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Danh mục",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isCategoriesLoading) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            userScrollEnabled = true
                        ) {
                            items(5) {
                                CategoryItemSkeleton()
                            }
                        }
                    } else if (categoriesError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(
                                onClick = {
                                    categoryViewModel.resetError()
                                    categoryViewModel.getCategories()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thử lại")
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            userScrollEnabled = true
                        ) {
                            items(categories) { category ->
                                CategoryItem(
                                    category = category,
                                    isSelected = selectedCategoryId == category.id,
                        onClick = {
                                        if (selectedCategoryId == category.id) {
                                            eventViewModel.clearCategorySelection()
                                        } else {
                                            eventViewModel.getEventsByCategory(category.id)
                                            onCategoryClick(category.id)
                                        }
                                    },
                                    fallbackIcon = Icons.Filled.Category
                                )
                            }
                        }
                    }
                }
            }
            
            // Events by Category (if a category is selected)
            if (selectedCategoryId != null) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val selectedCategory = categories.find { it.id == selectedCategoryId }
                        
                        Text(
                            text = "Sự kiện ${selectedCategory?.name ?: ""}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(
                        modifier = Modifier
                            .fillMaxWidth() 
                        ) {
                            when {
                                isCategoryEventsLoading -> {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        userScrollEnabled = true
                                    ) {
                                        items(3) {
                                            EventCardSkeleton(
                                                modifier = Modifier.width(220.dp)
                                            )
                                        }
                                    }
                                }
                                categoryEventsError != null -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Error,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            TextButton(
                                                onClick = {
                                                    eventViewModel.resetCategoryEventsError()
                                                    eventViewModel.getEventsByCategory(selectedCategoryId!!)
                                                }
                                            ) {
                                                Text("Thử lại")
                                            }
                                        }
                                    }
                                }
                                categoryEventsState is ResourceState.Success -> {
                                    val events = (categoryEventsState as ResourceState.Success<List<EventDto>>).data
                                    if (events.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Event,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                    Text(
                                                    text = "Không có sự kiện nào trong danh mục này",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            userScrollEnabled = true
                                        ) {
                                            items(events) { event ->
                                                EventCard(
                                                    event = event,
                                                    onClick = { onEventClick(event.id) },
                                                    modifier = Modifier.width(220.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Featured Events
                item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Sự kiện nổi bật",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                
                    when {
                        isFeaturedLoading -> {
                    LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                                userScrollEnabled = true
                            ) {
                                items(3) {
                                    EventCardSkeleton(
                                        modifier = Modifier.width(220.dp)
                                    )
                                }
                            }
                        }
                        featuredError != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    TextButton(
                                        onClick = {
                                            eventViewModel.resetFeaturedEventsError()
                                            eventViewModel.getFeaturedEvents()
                                        }
                                    ) {
                                        Text("Thử lại")
                                    }
                                }
                            }
                        }
                        featuredEventsState is ResourceState.Success -> {
                            val events = (featuredEventsState as ResourceState.Success<List<EventDto>>).data
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                userScrollEnabled = true
                            ) {
                                items(events) { event ->
                                    EventCard(
                                        event = event,
                                        onClick = { onEventClick(event.id) },
                                        modifier = Modifier.width(220.dp)
                                    )
                                }
                                }
                            }
                        }
                    }
                }
                
                // Upcoming Events
                item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Sự kiện sắp diễn ra",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    when {
                        isUpcomingLoading -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                repeat(3) {
                                    EventListItemSkeleton()
                                }
                            }
                        }
                        upcomingError != null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    TextButton(
                                        onClick = {
                                            eventViewModel.resetUpcomingEventsError()
                                            eventViewModel.getUpcomingEvents()
                                        }
                                    ) {
                                        Text("Thử lại")
                                    }
                                }
                            }
                        }
                        upcomingEventsState is ResourceState.Success -> {
                            val events = (upcomingEventsState as ResourceState.Success<List<EventDto>>).data
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                events.forEach { event ->
                                    EventListItem(
                        event = event,
                                        onClick = { onEventClick(event.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Bottom padding
            item {
                Spacer(modifier = Modifier.height(80.dp))
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