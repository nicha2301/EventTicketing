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
import java.text.NumberFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


enum class EventStatus {
    UPCOMING, ACTIVE, CANCELLED, COMPLETED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onTicketsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onExploreClick: () -> Unit,
    onCategoryClick: (String) -> Unit = {},
    eventViewModel: EventViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Collect states from ViewModels
    val featuredEventsState by eventViewModel.featuredEventsState.collectAsState()
    val upcomingEventsState by eventViewModel.upcomingEventsState.collectAsState()
    val categoriesState by categoryViewModel.categoriesState.collectAsState()
    val categoryEventsState by eventViewModel.categoryEventsState.collectAsState()
    val selectedCategoryId by eventViewModel.selectedCategoryId.collectAsState()
    
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
    
    // Load data when screen is displayed
    LaunchedEffect(Unit) {
        eventViewModel.getFeaturedEvents()
        eventViewModel.getUpcomingEvents()
        categoryViewModel.getCategories()
    }
    
    // Map category icons based on name (fallback)
    val categoryIcons = mapOf(
        "âm nhạc" to Icons.Filled.MusicNote,
        "thể thao" to Icons.Filled.SportsSoccer,
        "nghệ thuật" to Icons.Filled.Palette,
        "ẩm thực" to Icons.Filled.Restaurant,
        "công nghệ" to Icons.Filled.Computer,
        "giáo dục" to Icons.Filled.School,
        "kinh doanh" to Icons.Filled.Business,
        "sức khỏe" to Icons.Filled.HealthAndSafety,
        "giải trí" to Icons.Filled.Celebration,
        "văn hóa" to Icons.Filled.Museum,
        "other" to Icons.Filled.Category
    )
    
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
                                    fallbackIcon = categoryIcons[category.name.lowercase()] ?: Icons.Filled.Category
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
                                .height(250.dp) // Cố định chiều cao của container
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
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
    ) {
        Box(
                modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            if (category.iconUrl != null) {
                // Sử dụng iconUrl từ API
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(category.iconUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = category.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(32.dp),
                    error = painterResource(id = R.drawable.ic_category_default)
                )
            } else {
                // Sử dụng biểu tượng fallback nếu không có iconUrl
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = category.name,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
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
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event image with overlay gradient
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(event.featuredImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Status indicator
                val statusColor = when {
                    event.isFeatured -> MaterialTheme.colorScheme.primary
                    event.isFree -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.secondary
                }
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(8.dp)
                        .background(statusColor, CircleShape)
                )
            }
            
            // Event details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                // Title with possible badge
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (event.isFeatured) {
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Date
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                
                val startDate = try {
                    dateFormatter.parse(event.startDate)
                } catch (e: Exception) {
                    null
                }
                
                val formattedDate = startDate?.let { outputDateFormatter.format(it) } ?: event.startDate.split("T")[0]
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = event.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Price tag
                
                val priceText = when {
                    event.isFree -> "Miễn phí"
                    event.minTicketPrice != null -> {
                        formatPrice(event.minTicketPrice)
                    }
                    else -> "Chưa có giá"
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = priceText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    // Sử dụng DecimalFormat để định dạng số theo chuẩn Việt Nam
    val formatter = DecimalFormat("#,###")
    formatter.decimalFormatSymbols = DecimalFormatSymbols(Locale("vi", "VN"))
    return formatter.format(price) + " VNĐ"
} 