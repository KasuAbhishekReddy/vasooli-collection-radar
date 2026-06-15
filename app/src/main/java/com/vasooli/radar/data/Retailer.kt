package com.vasooli.radar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "retailers")
data class Retailer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopName: String,
    val ownerName: String,
    val phone: String,
    val area: String,
    val creditLimit: Double,
    val termDays: Int,
    val createdAt: Long
)
