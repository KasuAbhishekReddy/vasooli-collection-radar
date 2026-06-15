package com.vasooli.radar

import android.app.Application
import com.vasooli.radar.data.AppDatabase
import com.vasooli.radar.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class VasooliApp : Application() {
    lateinit var repository: Repository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = Repository(db.retailerDao(), db.ledgerDao())
        appScope.launch { repository.seedIfEmpty() }
    }
}
