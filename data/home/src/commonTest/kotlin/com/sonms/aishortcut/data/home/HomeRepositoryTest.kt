package com.sonms.aishortcut.data.home

import com.sonms.aishortcut.core.database.HomeVisitDao
import com.sonms.aishortcut.core.database.HomeVisitEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// In-memory stand-in for the Room DAO: single row keyed on id, REPLACE on
// conflict -- the same contract HomeVisitDao's annotations declare.
private class FakeHomeVisitDao : HomeVisitDao {
    private var row: HomeVisitEntity? = null

    override suspend fun get(): HomeVisitEntity? = row

    override suspend fun upsert(entity: HomeVisitEntity) {
        row = entity
    }
}

class HomeRepositoryTest {

    @Test
    fun countsUpFromZeroAcrossCalls() = runTest {
        val repo = HomeRepository(FakeHomeVisitDao())

        assertEquals(1, repo.recordVisit())
        assertEquals(2, repo.recordVisit())
        assertEquals(3, repo.recordVisit())
    }
}
