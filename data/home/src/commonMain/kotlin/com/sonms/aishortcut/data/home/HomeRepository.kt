package com.sonms.aishortcut.data.home

import com.sonms.aishortcut.core.database.HomeVisitDao
import com.sonms.aishortcut.core.database.HomeVisitEntity

// Room-backed visit counter. The DAO and AiShortCutDatabase behind it live in
// core:database; presentation:home talks to this, never to the DAO directly.
class HomeRepository(
    private val dao: HomeVisitDao,
) {
    suspend fun recordVisit(): Int {
        val next = (dao.get()?.count ?: 0) + 1
        dao.upsert(HomeVisitEntity(count = next))
        return next
    }
}
