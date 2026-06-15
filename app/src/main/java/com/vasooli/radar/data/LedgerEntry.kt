package com.vasooli.radar.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** SALE = goods given on credit (raises what the retailer owes).
 *  PAYMENT = cash/UPI received (reduces it).
 *  PROMISE = a "I'll pay by <date>" commitment, used to detect broken promises. */
enum class EntryType { SALE, PAYMENT, PROMISE }

@Entity(
    tableName = "ledger",
    foreignKeys = [ForeignKey(
        entity = Retailer::class,
        parentColumns = ["id"],
        childColumns = ["retailerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("retailerId")]
)
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val retailerId: Long,
    val type: EntryType,
    val amount: Double,
    val date: Long,
    /** For SALE: when payment is due. For PROMISE: the promised pay-by date. */
    val dueDate: Long? = null,
    val note: String? = null
)
