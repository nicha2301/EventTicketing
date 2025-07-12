package com.eventticketing.backend.repository

import com.eventticketing.backend.entity.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EventRepositoryIntegrationTest {

    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
        }
    }

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var eventRepository: EventRepository

    @Test
    fun `findByStatus returns events with specified status`() {
        // Arrange
        val user = createUser()
        val category = createCategory()
        val location = createLocation()
        
        val publishedEvent = createEvent(
            title = "Published Event",
            organizer = user,
            category = category,
            location = location,
            status = EventStatus.PUBLISHED
        )
        
        val draftEvent = createEvent(
            title = "Draft Event",
            organizer = user,
            category = category,
            location = location,
            status = EventStatus.DRAFT
        )
        
        entityManager.persist(user)
        entityManager.persist(category)
        entityManager.persist(location)
        entityManager.persist(publishedEvent)
        entityManager.persist(draftEvent)
        entityManager.flush()
        
        // Act
        val publishedEvents = eventRepository.findByStatus(EventStatus.PUBLISHED, PageRequest.of(0, 10))
        val draftEvents = eventRepository.findByStatus(EventStatus.DRAFT, PageRequest.of(0, 10))
        
        // Assert
        assertEquals(1, publishedEvents.totalElements)
        assertEquals("Published Event", publishedEvents.content[0].title)
        
        assertEquals(1, draftEvents.totalElements)
        assertEquals("Draft Event", draftEvents.content[0].title)
    }

    @Test
    fun `findByStartDateAfterAndStatusOrderByStartDateAsc returns upcoming events`() {
        // Arrange
        val user = createUser()
        val category = createCategory()
        val location = createLocation()
        
        val now = LocalDateTime.now()
        
        val upcomingEvent1 = createEvent(
            title = "Upcoming Event 1",
            organizer = user,
            category = category,
            location = location,
            status = EventStatus.PUBLISHED,
            startDate = now.plusDays(1)
        )
        
        val upcomingEvent2 = createEvent(
            title = "Upcoming Event 2",
            organizer = user,
            category = category,
            location = location,
            status = EventStatus.PUBLISHED,
            startDate = now.plusDays(2)
        )
        
        val pastEvent = createEvent(
            title = "Past Event",
            organizer = user,
            category = category,
            location = location,
            status = EventStatus.PUBLISHED,
            startDate = now.minusDays(1)
        )
        
        entityManager.persist(user)
        entityManager.persist(category)
        entityManager.persist(location)
        entityManager.persist(upcomingEvent1)
        entityManager.persist(upcomingEvent2)
        entityManager.persist(pastEvent)
        entityManager.flush()
        
        // Act
        val upcomingEvents = eventRepository.findByStartDateAfterAndStatusOrderByStartDateAsc(now, EventStatus.PUBLISHED, 10)
        
        // Assert
        assertEquals(2, upcomingEvents.size)
        assertEquals("Upcoming Event 1", upcomingEvents[0].title)
        assertEquals("Upcoming Event 2", upcomingEvents[1].title)
    }

    @Test
    fun `findByOrganizerId returns events by organizer`() {
        // Arrange
        val organizer1 = createUser(email = "organizer1@example.com")
        val organizer2 = createUser(email = "organizer2@example.com")
        val category = createCategory()
        val location = createLocation()
        
        val event1 = createEvent(
            title = "Event by Organizer 1",
            organizer = organizer1,
            category = category,
            location = location
        )
        
        val event2 = createEvent(
            title = "Another Event by Organizer 1",
            organizer = organizer1,
            category = category,
            location = location
        )
        
        val event3 = createEvent(
            title = "Event by Organizer 2",
            organizer = organizer2,
            category = category,
            location = location
        )
        
        entityManager.persist(organizer1)
        entityManager.persist(organizer2)
        entityManager.persist(category)
        entityManager.persist(location)
        entityManager.persist(event1)
        entityManager.persist(event2)
        entityManager.persist(event3)
        entityManager.flush()
        
        // Act
        val organizer1Events = eventRepository.findByOrganizerId(organizer1.id!!, PageRequest.of(0, 10))
        val organizer2Events = eventRepository.findByOrganizerId(organizer2.id!!, PageRequest.of(0, 10))
        
        // Assert
        assertEquals(2, organizer1Events.totalElements)
        assertEquals(1, organizer2Events.totalElements)
        
        assertTrue(organizer1Events.content.any { it.title == "Event by Organizer 1" })
        assertTrue(organizer1Events.content.any { it.title == "Another Event by Organizer 1" })
        assertEquals("Event by Organizer 2", organizer2Events.content[0].title)
    }

    private fun createUser(email: String = "test@example.com"): User {
        return User(
            email = email,
            password = "password",
            name = "Test User",
            role = UserRole.ORGANIZER
        )
    }

    private fun createCategory(): Category {
        return Category(
            name = "Test Category",
            description = "Test Category Description"
        )
    }

    private fun createLocation(): Location {
        return Location(
            name = "Test Location",
            address = "Test Address",
            city = "Test City",
            country = "Test Country",
            latitude = 10.0,
            longitude = 20.0
        )
    }

    private fun createEvent(
        title: String,
        organizer: User,
        category: Category,
        location: Location,
        status: EventStatus = EventStatus.PUBLISHED,
        startDate: LocalDateTime = LocalDateTime.now().plusDays(1),
        endDate: LocalDateTime = LocalDateTime.now().plusDays(2)
    ): Event {
        return Event(
            title = title,
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
            startDate = startDate,
            endDate = endDate,
            isPrivate = false,
            isFree = false,
            status = status
        )
    }
} 