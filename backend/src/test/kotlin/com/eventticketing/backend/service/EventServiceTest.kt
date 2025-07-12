package com.eventticketing.backend.service

import com.eventticketing.backend.dto.EventDto
import com.eventticketing.backend.entity.*
import com.eventticketing.backend.exception.ResourceNotFoundException
import com.eventticketing.backend.repository.*
import com.eventticketing.backend.service.impl.EventServiceImpl
import com.eventticketing.backend.util.FileStorageService
import com.eventticketing.backend.util.SecurityUtils
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class EventServiceTest {

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var categoryRepository: CategoryRepository

    @MockK
    private lateinit var locationRepository: LocationRepository

    @MockK
    private lateinit var fileStorageService: FileStorageService

    @MockK
    private lateinit var securityUtils: SecurityUtils

    @InjectMockKs
    private lateinit var eventService: EventServiceImpl

    private val eventId = UUID.randomUUID()
    private val organizerId = UUID.randomUUID()
    private val categoryId = UUID.randomUUID()
    private val locationId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getEventById returns event when found`() {
        // Arrange
        val event = createMockEvent()
        every { eventRepository.findById(eventId) } returns Optional.of(event)
        every { securityUtils.isCurrentUserOrAdmin(any()) } returns true

        // Act
        val result = eventService.getEventById(eventId)

        // Assert
        assertNotNull(result)
        assertEquals(eventId, result.id)
        assertEquals("Test Event", result.title)
        verify(exactly = 1) { eventRepository.findById(eventId) }
    }

    @Test
    fun `getEventById throws ResourceNotFoundException when event not found`() {
        // Arrange
        every { eventRepository.findById(eventId) } returns Optional.empty()

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            eventService.getEventById(eventId)
        }
        verify(exactly = 1) { eventRepository.findById(eventId) }
    }

    @Test
    fun `getAllEvents returns all events for admin`() {
        // Arrange
        val events = listOf(createMockEvent(), createMockEvent())
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(events, pageable, events.size.toLong())
        
        every { securityUtils.isAdmin() } returns true
        every { eventRepository.findAll(pageable) } returns page

        // Act
        val result = eventService.getAllEvents(pageable)

        // Assert
        assertEquals(2, result.totalElements)
        verify(exactly = 1) { securityUtils.isAdmin() }
        verify(exactly = 1) { eventRepository.findAll(pageable) }
        verify(exactly = 0) { eventRepository.findByStatus(any(), any()) }
    }

    @Test
    fun `getAllEvents returns published events for regular user`() {
        // Arrange
        val events = listOf(createMockEvent(), createMockEvent())
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(events, pageable, events.size.toLong())
        
        every { securityUtils.isAdmin() } returns false
        every { eventRepository.findByStatus(EventStatus.PUBLISHED, pageable) } returns page

        // Act
        val result = eventService.getAllEvents(pageable)

        // Assert
        assertEquals(2, result.totalElements)
        verify(exactly = 1) { securityUtils.isAdmin() }
        verify(exactly = 0) { eventRepository.findAll(any()) }
        verify(exactly = 1) { eventRepository.findByStatus(EventStatus.PUBLISHED, pageable) }
    }

    @Test
    fun `getFeaturedEvents returns featured events`() {
        // Arrange
        val events = listOf(createMockEvent(), createMockEvent())
        every { eventRepository.findFeaturedEvents(5) } returns events

        // Act
        val result = eventService.getFeaturedEvents(5)

        // Assert
        assertEquals(2, result.size)
        verify(exactly = 1) { eventRepository.findFeaturedEvents(5) }
    }

    private fun createMockEvent(): Event {
        val organizer = User(
            id = organizerId,
            email = "organizer@example.com",
            password = "password",
            name = "Organizer Name",
            role = UserRole.ORGANIZER
        )
        
        val category = Category(
            id = categoryId,
            name = "Test Category",
            description = "Test Category Description"
        )
        
        val location = Location(
            id = locationId,
            name = "Test Location",
            address = "Test Address",
            city = "Test City",
            country = "Test Country",
            latitude = 10.0,
            longitude = 20.0
        )
        
        return Event(
            id = eventId,
            title = "Test Event",
            description = "Test Description",
            shortDescription = "Test Short Description",
            organizer = organizer,
            category = category,
            location = location,
            address = "Test Event Address",
            city = "Test Event City",
            latitude = 10.0,
            longitude = 20.0,
            maxAttendees = 100,
            currentAttendees = 0,
            startDate = LocalDateTime.now().plusDays(1),
            endDate = LocalDateTime.now().plusDays(2),
            isPrivate = false,
            isFree = false,
            status = EventStatus.PUBLISHED,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
} 