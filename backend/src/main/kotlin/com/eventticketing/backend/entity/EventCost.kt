package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.annotations.UuidGenerator
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "event_costs")
class EventCost(
    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,
    
    @Column(name = "cost_type", nullable = false)
    var costType: String,
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    var amount: BigDecimal,
    
    @Column(name = "description")
    var description: String? = null,
    
    @Column(name = "vendor")
    var vendor: String? = null,
    
    @Column(name = "cost_date", nullable = false)
    var costDate: LocalDateTime = LocalDateTime.now(),
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class CostType {
    VENUE,
    MARKETING,
    STAFF,
    EQUIPMENT,
    CATERING,
    SECURITY,
    INSURANCE,
    PERMITS,
    TRANSPORTATION,
    OTHER
}
