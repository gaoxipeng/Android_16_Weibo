package com.example.myweibo

import com.example.myweibo.data.SearchHistoryEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SearchHistoryEntryTest {
    @Test fun userAndKeywordWithSameNameRemainSeparate() {
        assertNotEquals(SearchHistoryEntry("人民日报").key,
            SearchHistoryEntry("人民日报", "2803301701").key)
    }

    @Test fun userHistoryIdentitySurvivesRenameAndAvatarChanges() {
        assertEquals(SearchHistoryEntry("旧昵称", "123", "old").key,
            SearchHistoryEntry("新昵称", "123", "new").key)
    }
}
