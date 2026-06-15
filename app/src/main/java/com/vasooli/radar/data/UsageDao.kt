package com.vasooli.radar.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun record(day: UsageDay)

    @Query("SELECT day FROM usage_day")
    fun observeDays(): Flow<List<String>>
}
