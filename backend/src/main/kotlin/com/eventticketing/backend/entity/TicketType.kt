package com.eventticketing.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "ticket_types")
class TicketType(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    val id: UUID? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    var event: Event,
    
    @Column(nullable = false)
    var name: String,
    
    @Column(columnDefinition = "TEXT")
    var description: String? = null,
    
    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,
    
    @Column(nullable = false)
    var quantity: Int,
    
    @Column(name = "quantity_sold", nullable = false)
    var quantitySold: Int = 0,
    
    @Column(name = "sale_start_date", nullable = false)
    var saleStartDate: LocalDateTime,
    
    @Column(name = "sale_end_date", nullable = false)
    var saleEndDate: LocalDateTime,
    
    @OneToMany(mappedBy = "ticketType", cascade = [CascadeType.ALL])
    val tickets: MutableSet<Ticket> = mutableSetOf()
) 