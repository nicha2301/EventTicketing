package com.nicha.eventticketing.ui.screens.event

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.ui.components.app.AppButton
import com.nicha.eventticketing.ui.components.app.AppTextButton
import com.nicha.eventticketing.util.FormatUtils
import com.nicha.eventticketing.util.ImageUtils.getAllImageUrls
import com.nicha.eventticketing.viewmodel.EventViewModel
import com.nicha.eventticketing.viewmodel.TicketViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    onBuyTicketsClick: (String, String, String?) -> Unit,
    eventViewModel: EventViewModel,
    ticketViewModel: TicketViewModel,
    isOnline: Boolean
) {
    val eventState by eventViewModel.eventDetailState.collectAsState()
    val isDescriptionExpanded = remember { mutableStateOf(false) }
    val showTicketTypeSheet = remember { mutableStateOf(false) }

    LaunchedEffect(eventId) {
        eventViewModel.getEventById(eventId)
        ticketViewModel.getMyTicketsByEventId(eventId)
    }

    val allPendingTicketsState by ticketViewModel.allPendingTicketsState.collectAsState()
    LaunchedEffect(eventId) {
        ticketViewModel.getAllPendingTickets()
    }

    when (eventState) {
        is ResourceState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is ResourceState.Error -> {
            val errorMessage = (eventState as ResourceState.Error).message
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AppButton(
                        onClick = {
                            eventViewModel.resetEventDetailError()
                            eventViewModel.getEventById(eventId)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thử lại")
                    }
                }
            }
        }

        is ResourceState.Success -> {
            val event = (eventState as ResourceState.Success<EventDto>).data
            val imageUrls = event.getAllImageUrls()
            val pagerState = rememberPagerState(pageCount = { imageUrls.size.coerceAtLeast(1) })
            val cheapestTicketType = remember(event.ticketTypes) {
                event.ticketTypes?.minByOrNull { it.price }
            }
            val buyPriceText = FormatUtils.formatEventPrice(event.minTicketPrice, event.isFree)

            // Format event date
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val outputTimeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

            val startDate = try {
                dateFormatter.parse(event.startDate)
            } catch (e: Exception) {
                null
            }

            val endDate = try {
                dateFormatter.parse(event.endDate)
            } catch (e: Exception) {
                null
            }

            val formattedStartDate =
                startDate?.let { outputDateFormatter.format(it) } ?: event.startDate.split("T")[0]
            val formattedStartTime = startDate?.let { outputTimeFormatter.format(it) } ?: ""
            val formattedEndTime = endDate?.let { outputTimeFormatter.format(it) } ?: ""

            Box(modifier = Modifier.fillMaxSize()) {
                // Back Button - Fixed on Screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp, start = 16.dp)
                        .zIndex(1f)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // Hero section with image and overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(489.dp)
                        ) {
                            // Background image
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUrls.firstOrNull())
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Event hero image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Bottom gradient overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colorStops = arrayOf(
                                                0.0f to Color.White.copy(alpha = 0.0f),
                                                0.7f to Color.White.copy(alpha = 1.0f),
                                                1.0f to Color.White.copy(alpha = 1.0f)
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Event title (overlay on hero)
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                lineHeight = 28.8.sp
                            ),
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Location with icon
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color.Black,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${event.locationName}, ${event.address}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    lineHeight = 19.2.sp
                                ),
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Organizer
                        Text(
                            text = "Nhà tổ chức: ${event.organizerName}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                lineHeight = 19.2.sp
                            ),
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Event date and time
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Date and time",
                                tint = Color.Black,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (formattedStartTime.isNotEmpty()) {
                                    "$formattedStartDate • $formattedStartTime"
                                } else {
                                    formattedStartDate
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    lineHeight = 19.2.sp
                                ),
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(modifier = Modifier.height(29.dp))

                        // Event details section
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            // Description section
                            Text(
                                text = "Mô tả",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    lineHeight = 19.2.sp
                                ),
                                color = Color.Black.copy(alpha = 0.5f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = event.description,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 18.sp,
                                    lineHeight = 30.6.sp
                                ),
                                color = Color.Black.copy(alpha = 0.8f),
                                maxLines = if (isDescriptionExpanded.value) Int.MAX_VALUE else 5,
                                overflow = if (isDescriptionExpanded.value) TextOverflow.Visible else TextOverflow.Ellipsis
                            )

                            if (!isDescriptionExpanded.value && event.description.length > 200) {
                                AppTextButton(
                                    onClick = { isDescriptionExpanded.value = true }
                                ) {
                                    Text("Read more")
                                }
                            }

                            Spacer(modifier = Modifier.height(200.dp))
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color.White)
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(bottom = 120.dp)
                            .height(80.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color.White.copy(alpha = 0.0f),
                                        0.6f to Color.White.copy(alpha = 0.6f),
                                        1.0f to Color.White.copy(alpha = 1.0f)
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Share button (circular)
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFEE794A).copy(alpha = 0.2f)),
                            modifier = Modifier.size(74.dp)
                        ) {
                            IconButton(
                                onClick = { /* TODO: share */ },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color(0xFFEE794A),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Buy Ticket button
                        AppButton(
                            onClick = {
                                if (event.ticketTypes?.isNotEmpty() == true) {
                                    showTicketTypeSheet.value = true
                                }
                            },
                            enabled = event.ticketTypes?.isNotEmpty() == true && isOnline,
                            modifier = Modifier
                                .weight(1f)
                                .height(74.dp)
                        ) {
                            Text(
                                text = "Mua vé",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    letterSpacing = 0.72.sp,
                                    textAlign = TextAlign.Center
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Ticket Type Selection Bottom Sheet
            if (showTicketTypeSheet.value) {
                TicketTypeSelectionSheet(
                    ticketTypes = event.ticketTypes ?: emptyList(),
                    onDismiss = { showTicketTypeSheet.value = false },
                    onTicketTypeSelected = { ticketType ->
                        onBuyTicketsClick(event.id, ticketType.id, null)
                        showTicketTypeSheet.value = false
                    }
                )
            }
        }

        else -> {
            // Initial state - show nothing
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketTypeSelectionSheet(
    ticketTypes: List<TicketTypeDto>,
    onDismiss: () -> Unit,
    onTicketTypeSelected: (TicketTypeDto) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(Unit) {
        sheetState.show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Chọn loại vé",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(ticketTypes.size) { index ->
                    val ticketType = ticketTypes[index]
                    val formattedPrice = FormatUtils.formatPrice(ticketType.price)
                    val availableQuantity = ticketType.quantity - ticketType.quantitySold

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = availableQuantity > 0
                            ) {
                                onTicketTypeSelected(ticketType)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (availableQuantity > 0)
                                MaterialTheme.colorScheme.surface
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ticketType.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (availableQuantity > 0)
                                            MaterialTheme.colorScheme.onSurface
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (!ticketType.description.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = ticketType.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Text(
                                    text = formattedPrice,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFFEE794A)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (availableQuantity > 0) {
                                        "Còn lại: $availableQuantity vé"
                                    } else {
                                        "Hết vé"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (availableQuantity > 0)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.error
                                )

                                if (availableQuantity > 0) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Select",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
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