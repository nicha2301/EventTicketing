package com.nicha.eventticketing.ui.screens.event

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.data.remote.dto.ticket.TicketTypeDto
import com.nicha.eventticketing.domain.model.ResourceState
import com.nicha.eventticketing.viewmodel.EventViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.nicha.eventticketing.util.FormatUtils
import com.nicha.eventticketing.util.AnimationUtils
import com.nicha.eventticketing.util.TicketUtils
import com.nicha.eventticketing.data.remote.dto.ticket.TicketDto
import com.nicha.eventticketing.viewmodel.TicketViewModel
import com.nicha.eventticketing.util.ImageUtils
import com.nicha.eventticketing.util.ImageUtils.getFullFeaturedImageUrl
import com.nicha.eventticketing.util.ImageUtils.getFullImageUrls
import com.nicha.eventticketing.util.NetworkStatusObserver

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onBackClick: () -> Unit,
    onBuyTicketsClick: (String, String, String?) -> Unit,
    onViewTicketClick: (String) -> Unit = {},
    viewModel: EventViewModel = hiltViewModel(),
    ticketViewModel: TicketViewModel = hiltViewModel()
) {
    var showContent by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    val eventDetailState by viewModel.eventDetailState.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.getEventById(eventId)
    }

    val myTicketState by ticketViewModel.myTicketForEventState.collectAsState()
    LaunchedEffect(eventId) {
        ticketViewModel.getMyTicketsByEventId(eventId)
    }

    val allPendingTicketsState by ticketViewModel.allPendingTicketsState.collectAsState()
    LaunchedEffect(eventId) {
        ticketViewModel.getAllPendingTickets()
    }
    
    var selectedTicketType by remember { mutableStateOf<TicketTypeDto?>(null) }
    
    val existingUnpaidTicket = remember(selectedTicketType, allPendingTicketsState) {
        if (selectedTicketType != null) {
            val pendingOrders = (allPendingTicketsState as? ResourceState.Success)?.data
            val existingOrder = pendingOrders?.find { order ->
                order.eventId == eventId && 
                order.paymentStatus.equals("PENDING", ignoreCase = true) &&
                order.tickets.any { ticket -> 
                    ticket.ticketTypeId == selectedTicketType!!.id && 
                    ticket.status.equals("RESERVED", ignoreCase = true)
                }
            }
            
            existingOrder?.tickets?.find { ticket ->
                ticket.ticketTypeId == selectedTicketType!!.id && 
                ticket.status.equals("RESERVED", ignoreCase = true)
            }
        } else {
            null
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val context = LocalContext.current
    val isOnline by NetworkStatusObserver.observe(context).collectAsState(initial = true)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Chi tiết sự kiện",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",

                        )
                    }
                },
                actions = {
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        when (eventDetailState) {
            is ResourceState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ResourceState.Error -> {
                val errorMessage = (eventDetailState as ResourceState.Error).message
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
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.resetEventDetailError()
                                    delay(100)
                                    viewModel.getEventById(eventId)
                                }
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
                val event = (eventDetailState as ResourceState.Success<EventDto>).data
                var isDescriptionExpanded by remember { mutableStateOf(false) }

                // Lọc danh sách vé để chỉ hiển thị vé thường và vé VIP
                val filteredTicketTypes = remember(event.ticketTypes) {
                    TicketUtils.filterTicketTypes(event.ticketTypes)
                }

                val imageUrls = if (event.imageUrls.isNullOrEmpty() && event.featuredImageUrl != null) {
                    listOf(ImageUtils.getFullImageUrl(event.featuredImageUrl))
                } else {
                    ImageUtils.getFullImageUrls(event.imageUrls)
                }

                val pagerState = rememberPagerState(pageCount = { imageUrls.size })

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn() + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(durationMillis = 300)
                    )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                                    // Banner offline overlay trên ảnh
                    if (!isOnline) {
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
                // Image carousel with overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    // Images
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                        .data(imageUrls[page])
                                .crossfade(true)
                                .build(),
                            contentDescription = "Event image ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.2f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.2f)
                                    )
                                )
                            )
                    )

                    // Page indicators
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                                repeat(imageUrls.size) { iteration ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                            .size(
                                                width = if (pagerState.currentPage == iteration) 24.dp else 8.dp,
                                                height = 8.dp
                                            )
                                    .clip(CircleShape)
                                    .background(
                                                if (pagerState.currentPage == iteration)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            )
                                    )
                                }
                            }
                }

                // Event details
                Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Title
                        Text(
                            text = event.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Organizer
                            Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Organizer",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                        Text(
                                    text = "Tổ chức bởi: ${event.organizerName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Date and time
                            val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                            val outputDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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

                            val formattedStartDate = startDate?.let { outputDateFormatter.format(it) } ?: event.startDate.split("T")[0]
                            val formattedStartTime = startDate?.let { outputTimeFormatter.format(it) } ?: ""
                            val formattedEndTime = endDate?.let { outputTimeFormatter.format(it) } ?: ""

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = formattedStartDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (formattedStartTime.isNotEmpty()) {
                                    Text(
                                        text = " • $formattedStartTime",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (formattedEndTime.isNotEmpty() && formattedEndTime != formattedStartTime) {
                                        Text(
                                            text = " - $formattedEndTime",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Location
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "${event.locationName}, ${event.address}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Event status and attendance info
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Status chip
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = when (event.status) {
                                        "PUBLISHED" -> MaterialTheme.colorScheme.primaryContainer
                                        "CANCELLED" -> MaterialTheme.colorScheme.errorContainer
                                        "SOLD_OUT" -> MaterialTheme.colorScheme.tertiaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                            imageVector = when (event.status) {
                                                "PUBLISHED" -> Icons.Default.CheckCircle
                                                "CANCELLED" -> Icons.Default.Cancel
                                                "SOLD_OUT" -> Icons.Default.Warning
                                                else -> Icons.Default.Info
                                            },
                                            contentDescription = "Status",
                                            tint = when (event.status) {
                                                "PUBLISHED" -> MaterialTheme.colorScheme.onPrimaryContainer
                                                "CANCELLED" -> MaterialTheme.colorScheme.onErrorContainer
                                                "SOLD_OUT" -> MaterialTheme.colorScheme.onTertiaryContainer
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                            text = when (event.status) {
                                                "PUBLISHED" -> "Đang diễn ra"
                                                "CANCELLED" -> "Đã hủy"
                                                "SOLD_OUT" -> "Hết vé"
                                                else -> event.status
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            color = when (event.status) {
                                                "PUBLISHED" -> MaterialTheme.colorScheme.onPrimaryContainer
                                                "CANCELLED" -> MaterialTheme.colorScheme.onErrorContainer
                                                "SOLD_OUT" -> MaterialTheme.colorScheme.onTertiaryContainer
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }

                              Spacer(modifier = Modifier.height(8.dp))

                              HorizontalDivider(
                                  modifier = Modifier
                                      .fillMaxWidth()
                                      .padding(vertical = 16.dp),
                                  color = MaterialTheme.colorScheme.outlineVariant
                              )

                      // Description
                              Text(
                                  text = "Mô tả",
                                  style = MaterialTheme.typography.titleLarge,
                                  fontWeight = FontWeight.Bold
                              )

                              Spacer(modifier = Modifier.height(8.dp))

                              Text(
                                  text = event.description,
                                  style = MaterialTheme.typography.bodyMedium,
                                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                                  maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 5,
                                  overflow = if (isDescriptionExpanded) TextOverflow.Visible else TextOverflow.Ellipsis
                              )

                              if (event.description.length > 200) {
                                  TextButton(
                                      onClick = { isDescriptionExpanded = !isDescriptionExpanded },
                                      contentPadding = PaddingValues(0.dp)
                                  ) {
                              Text(
                                          text = if (isDescriptionExpanded) "Rút gọn" else "Xem thêm",
                                  color = MaterialTheme.colorScheme.primary,
                                          style = MaterialTheme.typography.labelLarge
                                      )
                                  }
                              }

                      Spacer(modifier = Modifier.height(16.dp))

                              // Ticket types
                              if (!filteredTicketTypes.isNullOrEmpty()) {
                                  Text(
                                      text = "Loại vé",
                                      style = MaterialTheme.typography.titleLarge,
                                      fontWeight = FontWeight.Bold
                                  )

                                  Spacer(modifier = Modifier.height(8.dp))

                                  filteredTicketTypes.forEach { ticketType ->
                                      TicketTypeItem(
                                          ticketType = ticketType,
                                          isSelected = selectedTicketType?.id == ticketType.id,
                                          onClick = {
                                              selectedTicketType = if (selectedTicketType?.id == ticketType.id) null else ticketType
                                          }
                                      )
                                      Spacer(modifier = Modifier.height(8.dp))
                                  }
                              } else {
                                  // No ticket types available
                                  Card(
                                      modifier = Modifier.fillMaxWidth(),
                                      colors = CardDefaults.cardColors(
                                          containerColor = MaterialTheme.colorScheme.errorContainer
                                      )
                                  ) {
                                      Row(
                                          modifier = Modifier
                                              .fillMaxWidth()
                                              .padding(16.dp),
                                          verticalAlignment = Alignment.CenterVertically
                                      ) {
                                          Icon(
                                              imageVector = Icons.Default.Info,
                                              contentDescription = "Info",
                                              tint = MaterialTheme.colorScheme.onErrorContainer
                                          )
                                          Spacer(modifier = Modifier.width(8.dp))
                                          Text(
                                              text = "Chưa có thông tin vé",
                                              color = MaterialTheme.colorScheme.onErrorContainer
                                          )
                                      }
                                  }
                              }

                              Spacer(modifier = Modifier.height(80.dp))
                          }
                      }
                  }

                  // Buy tickets button
                  Box(
                      modifier = Modifier.fillMaxSize(),
                      contentAlignment = Alignment.BottomCenter
                  ) {
                      when (myTicketState) {
                          is ResourceState.Loading -> {
                              // Hiển thị loading dưới cùng
                              CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                          }
                          is ResourceState.Success -> {
                              val ticket = (myTicketState as ResourceState.Success<TicketDto?>).data
                              if (ticket != null) {
                                  // Đã có vé hợp lệ
                                  Button(
                                      onClick = { onViewTicketClick(ticket.id) },
                                      modifier = Modifier
                                          .fillMaxWidth()
                                          .padding(16.dp),
                                      shape = RoundedCornerShape(28.dp)
                                  ) {
                                      Icon(
                                          imageVector = Icons.Default.ConfirmationNumber,
                                          contentDescription = "Xem chi tiết vé"
                                      )
                                      Spacer(modifier = Modifier.width(8.dp))
                                      Text("Xem chi tiết vé của bạn")
                                  }
                              } else if (!filteredTicketTypes.isNullOrEmpty()) {
                                  val hasPendingTicket = existingUnpaidTicket != null
                                  val buttonText = when {
                                      !isOnline -> "Không thể mua vé khi offline"
                                      selectedTicketType == null -> "Chọn loại vé để mua"
                                      hasPendingTicket -> "Thanh toán ngay"
                                      else -> "Mua vé ngay"
                                  }
                                  val buttonIcon = if (hasPendingTicket) Icons.Default.Payment else Icons.Default.ConfirmationNumber
                                  
                                  Surface(
                                      modifier = Modifier
                                          .fillMaxWidth()
                                          .padding(16.dp),
                                      shape = RoundedCornerShape(28.dp),
                                      color = if (selectedTicketType != null && isOnline) {
                                          if (hasPendingTicket) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                      } else MaterialTheme.colorScheme.surfaceVariant,
                                      shadowElevation = 6.dp
                                  ) {
                                      Row(
                                          modifier = Modifier
                                              .fillMaxWidth()
                                              .clickable(enabled = selectedTicketType != null && isOnline) {
                                                  if (isOnline) {
                                                      selectedTicketType?.let { ticketType ->
                                                          onBuyTicketsClick(event.id, ticketType.id, existingUnpaidTicket?.id)
                                                      }
                                                  }
                                              }
                                              .padding(16.dp),
                                          horizontalArrangement = Arrangement.Center,
                                          verticalAlignment = Alignment.CenterVertically
                                      ) {
                                          Icon(
                                              imageVector = buttonIcon,
                                              contentDescription = if (hasPendingTicket) "Pay now" else "Buy tickets",
                                              tint = if (selectedTicketType != null && isOnline) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                          )
                                          Spacer(modifier = Modifier.width(8.dp))
                                          Text(
                                              text = buttonText,
                                              style = MaterialTheme.typography.titleMedium,
                                              color = if (selectedTicketType != null && isOnline) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                          )
                                      }
                                  }
                              }
                          }
                          is ResourceState.Error -> {
                              if (!filteredTicketTypes.isNullOrEmpty()) {
                                  val hasPendingTicket = existingUnpaidTicket != null
                                  val buttonText = when {
                                      !isOnline -> "Không thể mua vé khi offline"
                                      selectedTicketType == null -> "Chọn loại vé để mua"
                                      hasPendingTicket -> "Thanh toán ngay"
                                      else -> "Mua vé ngay"
                                  }
                                  val buttonIcon = if (hasPendingTicket) Icons.Default.Payment else Icons.Default.ConfirmationNumber
                                  
                                  Surface(
                                      modifier = Modifier
                                          .fillMaxWidth()
                                          .padding(16.dp),
                                      shape = RoundedCornerShape(28.dp),
                                      color = if (selectedTicketType != null && isOnline) {
                                          if (hasPendingTicket) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                      } else MaterialTheme.colorScheme.surfaceVariant,
                                      shadowElevation = 6.dp
                                  ) {
                                      Row(
                                          modifier = Modifier
                                              .fillMaxWidth()
                                              .clickable(enabled = selectedTicketType != null && isOnline) {
                                                  if (isOnline) {
                                                      selectedTicketType?.let { ticketType ->
                                                          onBuyTicketsClick(event.id, ticketType.id, existingUnpaidTicket?.id)
                                                      }
                                                  }
                                              }
                                              .padding(16.dp),
                                          horizontalArrangement = Arrangement.Center,
                                          verticalAlignment = Alignment.CenterVertically
                                      ) {
                                          Icon(
                                              imageVector = buttonIcon,
                                              contentDescription = if (hasPendingTicket) "Pay now" else "Buy tickets",
                                              tint = if (selectedTicketType != null && isOnline) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                          )
                                          Spacer(modifier = Modifier.width(8.dp))
                                          Text(
                                              text = buttonText,
                                              style = MaterialTheme.typography.titleMedium,
                                              color = if (selectedTicketType != null && isOnline) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                          )
                                      }
                                  }
                              }
                          }
                          else -> {}
                      }
                  }
              }
              else -> {
                  // Initial state - show nothing
              }
          }
      }
  }

@Composable
fun TicketTypeItem(
    ticketType: TicketTypeDto,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val formattedPrice = FormatUtils.formatPrice(ticketType.price)

    val elevation = if (isSelected) 6.dp else 1.dp
    val scale = if (isSelected) 1.03f else 1f

    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface

    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outlineVariant

    val isVip = TicketUtils.isVipTicket(ticketType)
    val displayName = TicketUtils.getDisplayName(ticketType)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .scale(scale)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = borderColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Badge cho loại vé VIP
            if (isVip) {
                Surface(
                    shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700),  // Gold
                                        Color(0xFFFFA500)   // Orange
                                    )
                                ),
                                shape = RoundedCornerShape(bottomEnd = 12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VIP",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = if (isVip) 48.dp else 20.dp,
                        bottom = 20.dp,
                        start = 20.dp,
                        end = 20.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )

                        if (!ticketType.description.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = ticketType.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Giá vé với hiệu ứng đặc biệt cho vé VIP
                    if (isVip) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            Color(0xFFFFD700)
                                        )
                                    ),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = formattedPrice,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = formattedPrice,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Số lượng vé còn lại
                    val availableQuantity = ticketType.quantity - ticketType.quantitySold
                    val availabilityColor = when {
                        availableQuantity <= 0 -> MaterialTheme.colorScheme.error
                        availableQuantity < 10 -> Color(0xFFF57C00) // Orange
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    val availabilityText = when {
                        availableQuantity <= 0 -> "Hết vé"
                        availableQuantity < 10 -> "Chỉ còn $availableQuantity vé"
                        else -> "Còn $availableQuantity vé"
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = availabilityColor.copy(alpha = 0.1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = when {
                                    availableQuantity <= 0 -> Icons.Default.Warning
                                    availableQuantity < 10 -> Icons.Default.Timer
                                    else -> Icons.Default.ConfirmationNumber
                                },
                                contentDescription = null,
                                tint = availabilityColor,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = availabilityText,
                                style = MaterialTheme.typography.labelSmall,
                                color = availabilityColor
                            )
                        }
                    }
                }
            }
        }
    }
} 