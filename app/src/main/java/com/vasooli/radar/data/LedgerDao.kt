package com.vasooli.radar.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledger ORDER BY date")
    fun observeAll(): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger WHERE retailerId = :rid ORDER BY date")
    fun observeFor(rid: Long): Flow<List<LedgerEntry>>

    @Insert suspend fun insert(e: LedgerEntry): Long
    @Delete suspend fun delete(e: LedgerEntry)
}
