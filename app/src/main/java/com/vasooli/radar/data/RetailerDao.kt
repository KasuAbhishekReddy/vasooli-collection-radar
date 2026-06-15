package com.vasooli.radar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RetailerDao {
    @Query("SELECT * FROM retailers ORDER BY shopName")
    fun observeAll(): Flow<List<Retailer>>

    @Query("SELECT * FROM retailers WHERE id = :id")
    fun observe(id: Long): Flow<Retailer?>

    @Query("SELECT COUNT(*) FROM retailers")
    suspend fun count(): Int

    @Insert suspend fun insert(r: Retailer): Long
    @Update suspend fun update(r: Retailer)
    @Delete suspend fun delete(r: Retailer)
}
