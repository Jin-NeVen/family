package com.ntt.jin.family.data

import java.time.Instant
import java.util.Date

data class Room(
    val id: String,
    val name: String,
    var headerImageResId: Int,
    val createdAt: Instant
)
