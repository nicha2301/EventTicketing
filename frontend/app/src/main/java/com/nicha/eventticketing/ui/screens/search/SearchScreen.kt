package com.nicha.eventticketing.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nicha.eventticketing.data.remote.dto.category.CategoryDto
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.inputs.NeumorphicSearchField
import com.nicha.eventticketing.ui.components.LoadingIndicator
import com.nicha.eventticketing.ui.components.ErrorView
import com.nicha.eventticketing.viewmodel.CategoryViewModel
import com.nicha.eventticketing.viewmodel.SearchViewModel
import kotlinx.coroutines.launch
import java.util.Date
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.ui.text.style.TextAlign
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import com.nicha.eventticketing.util.FormatUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.CalendarToday
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onEventClick: (String) -> Unit,
    searchViewModel: SearchViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel()
) {
    val searchEventsState by searchViewModel.searchEventsState.collectAsState()
    val selectedCategoryId by searchViewModel.selectedCategoryId.collectAsState()
    val categoriesState by categoryViewModel.categoriesState.collectAsState()
    
    val coroutineScope = rememberCoroutineScope()
    
    var hasStartedSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    var showFilterDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        categoryViewModel.getCategories()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tìm kiếm") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Bộ lọc"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search field
            NeumorphicSearchField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    hasStartedSearch = true
                    searchViewModel.searchEventsWithDebounce(it)
                },
                placeholder = "Tìm kiếm sự kiện...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                onSearch = {
                    hasStartedSearch = true
                    coroutineScope.launch {
                        searchViewModel.searchEvents(searchQuery)
                    }
                },
                onKeyboardSearch = {
                    hasStartedSearch = true
                    coroutineScope.launch {
                        searchViewModel.searchEvents(searchQuery)
                    }
                }
            )
            
            // Category filters
            when (categoriesState) {
                is ResourceState.Success -> {
                    val categories = (categoriesState as ResourceState.Success<List<CategoryDto>>).data
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategoryId == category.id,
                                onClick = {
                                    searchViewModel.updateSelectedCategory(
                                        if (selectedCategoryId == category.id) null else category.id
                                    )
                                    if (hasStartedSearch) {
                                        searchViewModel.searchEvents(searchQuery)
                                    }
                                },
                                label = {
                                    Text(category.name)
                                }
                            )
                        }
                    }
                }
                is ResourceState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                else -> { /* Do nothing */ }
            }
            
            // Search results
            when (searchEventsState) {
                is ResourceState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                }
                is ResourceState.Success -> {
                    val events = (searchEventsState as ResourceState.Success<List<EventDto>>).data
                    if (events.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Không tìm thấy sự kiện nào phù hợp",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(events) { event ->
                                SearchResultItem(
                                    event = event,
                                    onClick = { onEventClick(event.id) }
                                )
                            }
                        }
                    }
                }
                is ResourceState.Error -> {
                    ErrorView(
                        message = (searchEventsState as ResourceState.Error).message,
                        onRetry = {
                            coroutineScope.launch {
                                searchViewModel.searchEvents(searchQuery)
                            }
                        }
                    )
                }
                is ResourceState.Initial -> {
                    if (!hasStartedSearch) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Nhập từ khóa để tìm kiếm sự kiện",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Filter dialog
    if (showFilterDialog) {
        FilterDialog(
            onDismiss = { showFilterDialog = false },
            onApply = {
                showFilterDialog = false
                if (hasStartedSearch || searchQuery.isNotEmpty()) {
                    searchViewModel.searchEvents(searchQuery)
                }
            },
            viewModel = searchViewModel
        )
    }
}

@Composable
fun SearchResultItem(
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
                        .data(event.featuredImageUrl)
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
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Date
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
                    
                    // Location
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
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Price
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text(
                        text = FormatUtils.formatEventPrice(event.minTicketPrice, event.isFree, "Từ "),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    viewModel: SearchViewModel
) {
    val priceRange by viewModel.priceRange.collectAsState()
    val dateRange by viewModel.dateRange.collectAsState()
    
    var minPriceInput by remember { mutableStateOf(priceRange.first?.toString() ?: "") }
    var maxPriceInput by remember { mutableStateOf(priceRange.second?.toString() ?: "") }
    var startDateInput by remember { mutableStateOf(dateRange.first ?: "") }
    var endDateInput by remember { mutableStateOf(dateRange.second ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bộ lọc tìm kiếm") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Khoảng giá", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = minPriceInput,
                        onValueChange = { minPriceInput = it },
                        label = { Text("Giá tối thiểu") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = maxPriceInput,
                        onValueChange = { maxPriceInput = it },
                        label = { Text("Giá tối đa") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Thời gian", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = startDateInput,
                        onValueChange = { startDateInput = it },
                        label = { Text("Từ ngày (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = endDateInput,
                        onValueChange = { endDateInput = it },
                        label = { Text("Đến ngày (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.updatePriceRange(
                        minPriceInput.toDoubleOrNull(),
                        maxPriceInput.toDoubleOrNull()
                    )
                    
                    val startDate = if (startDateInput.isBlank()) null else startDateInput
                    val endDate = if (endDateInput.isBlank()) null else endDateInput
                    
                    if (startDate != null || endDate != null) {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        val startDateObj = startDate?.let { runCatching { sdf.parse(it) }.getOrNull() }
                        val endDateObj = endDate?.let { runCatching { sdf.parse(it) }.getOrNull() }
                        viewModel.updateDateRange(startDateObj, endDateObj)
                    } else {
                        viewModel.updateDateRange(null, null)
                    }
                    
                    viewModel.searchEvents("")
                    
                    onApply()
                }
            ) {
                Text("Áp dụng")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.resetFilters()
                    onDismiss()
                }
            ) {
                Text("Xóa bộ lọc")
            }
        }
    )
}
