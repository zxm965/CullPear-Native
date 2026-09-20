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

    @Test
    fun parsesLatestStableReleaseFromAtomFeed() {
        val feed = """
            <feed>
              <entry>
                <id>tag:github.com,2008:Repository/1/v1.2.0-beta.1</id>
                <content type="html">&lt;p&gt;测试版本&lt;/p&gt;</content>
              </entry>
              <entry>
                <id>tag:github.com,2008:Repository/1/v1.1.1</id>
                <content type="html">&lt;h2&gt;更新内容&lt;/h2&gt;&lt;ul&gt;&lt;li&gt;修复检查更新 403&lt;/li&gt;&lt;/ul&gt;</content>
              </entry>
            </feed>
        """.trimIndent()

        val update = AppUpdateManager.parseLatestReleaseFeed(feed)

        assertEquals("1.1.1", update?.version)
        assertEquals(
            "https://github.com/zxm965/CullPear-Native/releases/download/v1.1.1/CullPear-1.1.1.apk",
            update?.apkUrl,
        )
        assertEquals("更新内容\n修复检查更新 403", update?.releaseNotes)
    }
}
