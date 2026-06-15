package com.vasooli.radar.data

class Repository(
    private val retailerDao: RetailerDao,
    private val ledgerDao: LedgerDao
) {
    val retailers = retailerDao.observeAll()
    val ledger = ledgerDao.observeAll()

    fun retailer(id: Long) = retailerDao.observe(id)
    fun ledgerFor(id: Long) = ledgerDao.observeFor(id)

    suspend fun addRetailer(r: Retailer): Long = retailerDao.insert(r)
    suspend fun updateRetailer(r: Retailer) = retailerDao.update(r)
    suspend fun deleteRetailer(r: Retailer) = retailerDao.delete(r)
    suspend fun deleteEntry(e: LedgerEntry) = ledgerDao.delete(e)

    suspend fun addSale(retailerId: Long, amount: Double, date: Long, dueDate: Long, note: String?) =
        ledgerDao.insert(LedgerEntry(retailerId = retailerId, type = EntryType.SALE, amount = amount, date = date, dueDate = dueDate, note = note))

    suspend fun addPayment(retailerId: Long, amount: Double, date: Long, note: String?) =
        ledgerDao.insert(LedgerEntry(retailerId = retailerId, type = EntryType.PAYMENT, amount = amount, date = date, note = note))

    suspend fun addPromise(retailerId: Long, amount: Double, promiseDate: Long, recordedAt: Long, note: String?) =
        ledgerDao.insert(LedgerEntry(retailerId = retailerId, type = EntryType.PROMISE, amount = amount, date = recordedAt, dueDate = promiseDate, note = note))

    suspend fun seedIfEmpty() {
        if (retailerDao.count() == 0) SeedData.seed(this)
    }
}
