package com.vasooli.radar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One row per calendar day the app was actually opened (local date "yyyy-MM-dd"). */
@Entity(tableName = "usage_day")
data class UsageDay(
    @PrimaryKey val day: String
)
