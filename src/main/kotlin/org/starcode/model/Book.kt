package org.starcode.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "book")
data class Book(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(nullable = false)
    var title: String = "",
    @Column(nullable = false)
    var author: String = "",
    @Column(nullable = true, name = "launch_date")
    var launchDate: Date? = null,
    @Column(nullable = false)
    var price: Double = 0.0,
)
