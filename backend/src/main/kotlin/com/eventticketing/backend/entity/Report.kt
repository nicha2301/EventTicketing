package com.eventticketing.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reports")
class Report(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val type: String, // REVENUE, ATTENDANCE, SALES

    @Column(nullable = true)
    val description: String?,

    @Column(nullable = false)
    val dateGenerated: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    val parameters: String?, // JSON string of parameters used to generate the report

    @Column(nullable = true)
    val resultData: String?, // JSON string of the report data

    @Column(nullable = true)
    val filePath: String?, // Path to the exported file if any

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val generatedBy: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = true)
    val event: Event? = null,
) 