package com.example.myweibo

import com.example.myweibo.data.AppCacheCategory.*
import com.example.myweibo.data.AppCacheFiles
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class AppCacheFilesTest {
    @Test fun countsAndClearsOnlySelectedCacheCategory() {
        val workspace = Files.createTempDirectory("weibo-cache-test").toFile()
        try {
            val cache = File(workspace, "cache").apply { mkdirs() }
            val image = File(cache, "remote-image-bytes/image").apply { parentFile?.mkdirs(); writeBytes(ByteArray(100)) }
            val video = File(cache, "weibo-video/span").apply { parentFile?.mkdirs(); writeBytes(ByteArray(200)) }
            val account = File(workspace, "files/accounts.json").apply { parentFile?.mkdirs(); writeText("keep") }
            val sizes = AppCacheFiles.measure(cache)
            assertEquals(100L, sizes[Images])
            assertEquals(200L, sizes[Videos])
            assertEquals(300L, sizes.total)
            assertEquals(0, AppCacheFiles.clearFiles(cache, setOf(Images)))
            assertFalse(image.exists())
            assertTrue(video.exists())
            assertEquals("keep", account.readText())
        } finally { workspace.deleteRecursively() }
    }

    @Test fun preservesPendingWritesAndRecentShares() {
        val cache = Files.createTempDirectory("weibo-cache-busy-test").toFile()
        try {
            val pending = File(cache, "remote-image-bytes/a.pending").apply { parentFile?.mkdirs(); writeText("busy") }
            val recent = File(cache, "share_recent.jpg").apply { writeText("busy") }
            val old = File(cache, "share_old.jpg").apply { writeText("old"); setLastModified(1L) }
            AppCacheFiles.clearFiles(cache, setOf(Images, Temporary))
            assertTrue(pending.exists())
            assertTrue(recent.exists())
            assertFalse(old.exists())
        } finally { cache.deleteRecursively() }
    }
}
