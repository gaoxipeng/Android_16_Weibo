package com.example.myweibo

import com.example.myweibo.data.FeedImage
import com.example.myweibo.ui.profileAlbumMedia
import com.example.myweibo.ui.shouldRepositionAlbumViewer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileAlbumMediaTest {
    @Test fun videoEntriesDoNotShiftPhotoReturnRows() {
        val items = (0..8).flatMap { index ->
            listOf(
                FeedImage("video-$index", "", "", type = "video", videoStreamUrl = "video.mp4"),
                FeedImage("photo-$index", "", "photo-$index.jpg"),
            )
        }
        val photoRows = profileAlbumMedia(items).chunked(3)
        assertEquals(3, photoRows.size)
        assertEquals(listOf("photo-3", "photo-4", "photo-5"), photoRows[1].map { it.id })
        assertEquals(9, profileAlbumMedia(items, videos = true).size)
    }

    @Test fun returningToTheClickedPhotoNeverRepositionsTheAlbum() {
        assertFalse(shouldRepositionAlbumViewer(30, 30, true))
        assertFalse(shouldRepositionAlbumViewer(30, 30, false))
    }

    @Test fun onlyAnotherOffscreenPhotoNeedsRepositioning() {
        assertFalse(shouldRepositionAlbumViewer(30, 31, true))
        assertTrue(shouldRepositionAlbumViewer(30, 60, false))
        assertTrue(shouldRepositionAlbumViewer(30, 0, false))
    }
}
