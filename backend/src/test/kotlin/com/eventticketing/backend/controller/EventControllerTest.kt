package com.eventticketing.backend.controller

import com.eventticketing.backend.dto.EventDto
import com.eventticketing.backend.security.JwtAuthenticationFilter
import com.eventticketing.backend.security.UserPrincipal
import com.eventticketing.backend.service.EventService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(
    controllers = [EventController::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [JwtAuthenticationFilter::class]
        )
    ]
)
class EventControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var eventService: EventService

    @Test
    @WithMockUser(roles = ["USER"])
    fun `getEventById returns event when found`() {
        // Arrange
        val eventId = UUID.randomUUID()
        val eventDto = createEventDto(eventId)
        
        every { eventService.getEventById(eventId) } returns eventDto

        // Act & Assert
        mockMvc.perform(
            get("/api/events/{id}", eventId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(eventId.toString()))
            .andExpect(jsonPath("$.title").value("Test Event"))
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `getAllEvents returns paginated events`() {
        // Arrange
        val events = listOf(
            createEventDto(UUID.randomUUID(), "Event 1"),
            createEventDto(UUID.randomUUID(), "Event 2")
        )
        
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(events, pageable, events.size.toLong())
        
        every { eventService.getAllEvents(any()) } returns page

        // Act & Assert
        mockMvc.perform(
            get("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].title").value("Event 1"))
            .andExpect(jsonPath("$.content[1].title").value("Event 2"))
            .andExpect(jsonPath("$.totalElements").value(2))
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `getFeaturedEvents returns featured events`() {
        // Arrange
        val events = listOf(
            createEventDto(UUID.randomUUID(), "Featured Event 1"),
            createEventDto(UUID.randomUUID(), "Featured Event 2")
        )
        
        every { eventService.getFeaturedEvents(any()) } returns events

        // Act & Assert
        mockMvc.perform(
            get("/api/events/featured")
                .contentType(MediaType.APPLICATION_JSON)
                .param("limit", "5")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].title").value("Featured Event 1"))
            .andExpect(jsonPath("$[1].title").value("Featured Event 2"))
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `getUpcomingEvents returns upcoming events`() {
        // Arrange
        val events = listOf(
            createEventDto(UUID.randomUUID(), "Upcoming Event 1"),
            createEventDto(UUID.randomUUID(), "Upcoming Event 2")
        )
        
        every { eventService.getUpcomingEvents(any()) } returns events

        // Act & Assert
        mockMvc.perform(
            get("/api/events/upcoming")
                .contentType(MediaType.APPLICATION_JSON)
                .param("limit", "5")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].title").value("Upcoming Event 1"))
            .andExpect(jsonPath("$[1].title").value("Upcoming Event 2"))
    }

    @Test
    @WithMockUser(roles = ["ORGANIZER"])
    fun `getEventsByOrganizer returns events for organizer`() {
        // Arrange
        val organizerId = UUID.randomUUID()
        val events = listOf(
            createEventDto(UUID.randomUUID(), "Organizer Event 1"),
            createEventDto(UUID.randomUUID(), "Organizer Event 2")
        )
        
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(events, pageable, events.size.toLong())
        
        // Set up authentication with organizer ID
        val authorities = listOf(SimpleGrantedAuthority("ROLE_ORGANIZER"))
        val userPrincipal = UserPrincipal(organizerId, "organizer@example.com", "password", authorities)
        val authentication = UsernamePasswordAuthenticationToken(userPrincipal, null, authorities)
        SecurityContextHolder.getContext().authentication = authentication
        
        every { eventService.getEventsByOrganizer(organizerId, any()) } returns page

        // Act & Assert
        mockMvc.perform(
            get("/api/events/organizer/{organizerId}", organizerId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].title").value("Organizer Event 1"))
            .andExpect(jsonPath("$.content[1].title").value("Organizer Event 2"))
            .andExpect(jsonPath("$.totalElements").value(2))
    }

    private fun createEventDto(
        id: UUID,
        title: String = "Test Event"
    ): EventDto {
        return EventDto(
            id = id,
            title = title,
            description = "Test Description",
            shortDescription = "Test Short Description",
            organizerId = UUID.randomUUID(),
            organizerName = "Test Organizer",
            categoryId = UUID.randomUUID(),
            categoryName = "Test Category",
            locationId = UUID.randomUUID(),
            locationName = "Test Location",
            address = "Test Address",
            city = "Test City",
            latitude = 10.0,
            longitude = 20.0,
            maxAttendees = 100,
            currentAttendees = 0,
            startDate = LocalDateTime.now().plusDays(1),
            endDate = LocalDateTime.now().plusDays(2),
            isPrivate = false,
            isFree = false,
            status = "PUBLISHED",
            primaryImage = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
} 