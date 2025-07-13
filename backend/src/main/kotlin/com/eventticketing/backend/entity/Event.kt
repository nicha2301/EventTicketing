package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "events",
    indexes = [
        Index(name = "idx_event_organizer", columnList = "organizer_id"),
        Index(name = "idx_event_category", columnList = "category_id"),
        Index(name = "idx_event_location", columnList = "location_id"),
        Index(name = "idx_event_status", columnList = "status"),
        Index(name = "idx_event_start_date", columnList = "start_date"),
        Index(name = "idx_event_city", columnList = "city"),
        Index(name = "idx_event_featured", columnList = "is_featured"),
        Index(name = "idx_event_location_coords", columnList = "latitude, longitude")
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, length = 255)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Column(name = "short_description", nullable = false)
    var shortDescription: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    var organizer: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    var location: Location,

    @Column(nullable = false)
    var address: String,

    @Column(nullable = false)
    var city: String,

    @Column(nullable = false)
    var latitude: Double,

    @Column(nullable = false)
    var longitude: Double,

    @Column(name = "max_attendees", nullable = false)
    var maxAttendees: Int,

    @Column(name = "current_attendees", nullable = false)
    var currentAttendees: Int = 0,

    @Column(name = "featured_image_url")
    var featuredImageUrl: String? = null,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDateTime,

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDateTime,

    @Column(name = "is_private", nullable = false)
    var isPrivate: Boolean = false,

    @Column(name = "is_featured", nullable = false)
    var isFeatured: Boolean = false,

    @Column(name = "is_free", nullable = false)
    var isFree: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: EventStatus = EventStatus.DRAFT,

    @Column(name = "cancellation_reason")
    var cancellationReason: String? = null,

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    var images: MutableList<EventImage> = mutableListOf(),

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ticketTypes: MutableList<TicketType> = mutableListOf(),
    
    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<Comment> = mutableListOf(),
    
    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    var ratings: MutableList<Rating> = mutableListOf(),
    
    @Column(name = "average_rating")
    var averageRating: Double = 0.0,
    
    @Column(name = "rating_count")
    var ratingCount: Int = 0,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // Phương thức tiện ích để thêm hình ảnh
    fun addImage(image: EventImage) {
        images.add(image)
        image.event = this
    }

    // Phương thức tiện ích để thêm loại vé
    fun addTicketType(ticketType: TicketType) {
        ticketTypes.add(ticketType)
        ticketType.event = this
    }
    
    // Phương thức tiện ích để thêm bình luận
    fun addComment(comment: Comment) {
        comments.add(comment)
        comment.event = this
    }
    
    // Phương thức tiện ích để thêm đánh giá
    fun addRating(rating: Rating) {
        ratings.add(rating)
        rating.event = this
        
        // Cập nhật đánh giá trung bình
        updateAverageRating()
    }
    
    // Phương thức cập nhật đánh giá trung bình
    fun updateAverageRating() {
        val approvedRatings = ratings.filter { it.status == RatingStatus.APPROVED }
        ratingCount = approvedRatings.size
        
        if (ratingCount > 0) {
            averageRating = approvedRatings.map { it.score.toDouble() }.average()
        } else {
            averageRating = 0.0
        }
    }
}

@Entity
@Table(
    name = "event_images",
    indexes = [
        Index(name = "idx_event_image_event", columnList = "event_id"),
        Index(name = "idx_event_image_primary", columnList = "is_primary")
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class EventImage(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    var url: String,

    @Column(name = "is_primary", nullable = false)
    var isPrimary: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    var event: Event? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

enum class EventStatus {
    DRAFT,       // Bản nháp, chưa công bố
    PUBLISHED,   // Đã công bố, đang bán vé
    CANCELLED,   // Đã hủy
    COMPLETED    // Đã diễn ra
}