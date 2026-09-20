package com.zxm965.cullpear.core.update

import org.junit.Assert.assertEquals
import org.junit.Test

class AppUpdateManagerTest {
    @Test
    fun comparesSemanticVersions() {
        assertEquals(1, AppUpdateManager.compareVersions("1.2.0", "1.1.9"))
        assertEquals(0, AppUpdateManager.compareVersions("1.2", "1.2.0"))
        assertEquals(-1, AppUpdateManager.compareVersions("1.9.9", "2.0.0"))
    }

    @Test
    fun ignoresPrereleaseSuffixForUpdateOrdering() {
        assertEquals(0, AppUpdateManager.compareVersions("2.0.0-beta.1", "2.0.0"))
    }

    @Test
    fun formatsGitHubReleaseNotesForDialog() {
        val markdown = """
            ## What's Changed
            * [优化更新体验](https://example.com/change)
            **Full Changelog**: https://example.com/compare
        """.trimIndent()

        assertEquals("What's Changed\n优化更新体验", AppUpdateManager.formatReleaseNotes(markdown))
    }
}
