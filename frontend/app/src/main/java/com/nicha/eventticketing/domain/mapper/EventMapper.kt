package com.nicha.eventticketing.domain.mapper

import com.nicha.eventticketing.data.remote.dto.event.EventDto
import com.nicha.eventticketing.domain.model.Category
import com.nicha.eventticketing.domain.model.Event
import com.nicha.eventticketing.domain.model.EventPricing
import com.nicha.eventticketing.domain.model.EventStatus
import com.nicha.eventticketing.domain.model.Rating
import com.nicha.eventticketing.domain.model.TicketInfo
import com.nicha.eventticketing.util.ImageUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper để chuyển đổi giữa EventDto và Event domain model
 */
@Singleton
class EventMapper @Inject constructor() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    fun mapToDomainModel(dto: EventDto): Event {
        return Event(
            id = dto.id,
            title = dto.title,
            description = dto.description,
            startDate = parseDate(dto.startDate),
            endDate = parseDate(dto.endDate),
            status = EventStatus.fromString(dto.status),
            address = dto.address,
            latitude = dto.latitude,
            longitude = dto.longitude,
            location = dto.locationName,
            organizerId = dto.organizerId,
            organizerName = dto.organizerName,
            category = dto.categoryId.let {
                Category(
                    id = it,
                    name = dto.categoryName,
                    description = null,
                    iconUrl = null
                )
            },
            featuredImageUrl = ImageUtils.getFullImageUrl(dto.featuredImageUrl),
            imageUrl = ImageUtils.getFullImageUrl(dto.featuredImageUrl),
            pricing = EventPricing(
                minPrice = dto.minTicketPrice,
                maxPrice = dto.maxTicketPrice,
                basePrice = dto.minTicketPrice ?: 0.0
            ),
            ticketInfo = TicketInfo(
                ticketsSold = dto.currentAttendees,
                totalTickets = dto.maxAttendees,
                availableSeats = dto.maxAttendees - dto.currentAttendees,
                totalSeats = dto.maxAttendees
            ),
            rating = Rating(
                average = 0.0, // Không có trong EventDto
                count = 0 // Không có trong EventDto
            ),
            createdAt = parseDate(dto.createdAt),
            updatedAt = parseDate(dto.updatedAt),
            isFavorite = false // Mặc định là false
        )
    }

    private fun parseDate(dateString: String): Date {
        return try {
            dateFormat.parse(dateString) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
} 