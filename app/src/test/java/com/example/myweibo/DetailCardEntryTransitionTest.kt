package com.example.myweibo

import com.example.myweibo.ui.detailEntrySnapshotAlpha
import com.example.myweibo.ui.detailCardReturnAmount
import com.example.myweibo.ui.DetailCardOvershoot
import org.junit.Assert.assertEquals
import org.junit.Test

class DetailCardEntryTransitionTest {
    @Test fun keepsSourceUntilTheDetailIsLaidOut() {
        assertEquals(1f, detailEntrySnapshotAlpha(0.8f, false), 0.0001f)
    }

    @Test fun handsOffSmoothlyAtTheBeginningOfEntry() {
        assertEquals(1f, detailEntrySnapshotAlpha(0f, true), 0.0001f)
        assertEquals(0.5f, detailEntrySnapshotAlpha(0.11f, true), 0.0001f)
        assertEquals(0f, detailEntrySnapshotAlpha(0.22f, true), 0.0001f)
        assertEquals(0f, detailEntrySnapshotAlpha(1f, true), 0.0001f)
    }

    @Test fun returnCanOvershootAndSettlesExactlyOnSource() {
        assertEquals(1f + DetailCardOvershoot, detailCardReturnAmount(-DetailCardOvershoot), 0.0001f)
        assertEquals(1f, detailCardReturnAmount(0f), 0.0001f)
        assertEquals(0f, detailCardReturnAmount(1f), 0.0001f)
        assertEquals(1f + DetailCardOvershoot, detailCardReturnAmount(-1f), 0.0001f)
    }
}
