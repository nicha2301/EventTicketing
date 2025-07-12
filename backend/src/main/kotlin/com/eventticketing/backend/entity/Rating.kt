package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "ratings",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "event_id"], name = "uk_user_event_rating")
    ]
)
@EntityListeners(AuditingEntityListener::class)
data class Rating(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    var score: Int,  // 1-5 sao

    @Column(columnDefinition = "TEXT")
    var review: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    var event: Event,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RatingStatus = RatingStatus.APPROVED,

    @Column(name = "is_reported", nullable = false)
    var isReported: Boolean = false,

    @Column(name = "report_reason")
    var reportReason: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class RatingStatus {
    APPROVED,   // Đã được phê duyệt
    REJECTED,   // Đã bị từ chối
    HIDDEN      // Đã bị ẩn
} 