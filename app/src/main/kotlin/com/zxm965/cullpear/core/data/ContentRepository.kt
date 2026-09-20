package com.zxm965.cullpear.core.data

import com.zxm965.cullpear.core.model.AboutContent
import com.zxm965.cullpear.core.model.Banner
import com.zxm965.cullpear.core.model.BlogPost
import com.zxm965.cullpear.core.model.ContactContent
import com.zxm965.cullpear.core.model.ContactInfo
import com.zxm965.cullpear.core.model.HomeContent
import com.zxm965.cullpear.core.model.ProcessStep
import com.zxm965.cullpear.core.model.Profile
import com.zxm965.cullpear.core.model.Project
import com.zxm965.cullpear.core.model.SiteInfo
import com.zxm965.cullpear.core.model.SiteShell
import com.zxm965.cullpear.core.model.SocialLink
import com.zxm965.cullpear.core.model.StackGroup
import com.zxm965.cullpear.core.network.ContentApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ContentRepository(private val api: ContentApiClient) {
    suspend fun getHome(): HomeContent = coroutineScope {
        val shell = async { getShell() }
        val banners = async { parseBanners(api.get("site/banners").dataArray()) }
        val projects = async { parseProjects(api.get("works", pageQuery()).dataObject().optJSONArray("items")) }
        HomeContent(shell.await(), banners.await(), projects.await().take(3))
    }

    suspend fun getProjects(): List<Project> =
        parseProjects(api.get("works", pageQuery()).dataObject().optJSONArray("items"))

    suspend fun getProject(slug: String): Project? =
        api.get("works/${encodePath(slug)}").dataObject().optJSONObject("item")?.let(::parseProject)

    suspend fun getPosts(): List<BlogPost> =
        parsePosts(api.get("blogs", pageQuery()).dataObject().optJSONArray("items"))

    suspend fun getPost(slug: String): BlogPost? =
        api.get("blogs/${encodePath(slug)}").dataObject().optJSONObject("item")?.let(::parsePost)

    suspend fun getContact(): ContactContent = coroutineScope {
        val shell = async { getShell() }
        val data = api.get("site/contact").dataObject()
        val currentShell = shell.await()
        val contact = ContactInfo(
            name = data.text("contact_name", currentShell.profile.name),
            role = data.text("contact_role", currentShell.profile.role),
            email = data.text("contact_email"),
            phone = data.text("contact_phone"),
            wechat = data.text("contact_wechat"),
            address = data.text("contact_address"),
            location = data.text("location", currentShell.profile.location),
            availability = data.text("availability", currentShell.profile.availability),
            resumeUrl = data.text("resume_url"),
            businessHours = data.text("business_hours"),
            mapUrl = data.text("map_embed_url"),
        )
        ContactContent(currentShell, contact, parseSocialLinks(data.optJSONArray("social_links")))
    }

    fun getAbout(): AboutContent = AboutContent(
        stack = listOf("Kotlin", "Jetpack Compose", "TypeScript", "React", "Vue", "Node.js", "Expo"),
        groups = listOf(
            StackGroup("界面表达", "把内容、交互和视觉层次整理成清晰稳定的体验。", listOf("Compose", "React", "Vue")),
            StackGroup("产品整理", "围绕作品、文章和联系方式形成可持续更新的内容结构。", listOf("Kotlin", "TypeScript", "Expo")),
            StackGroup("服务连接", "让内容录入、列表展示和外部链接保持顺畅统一。", listOf("REST", "Node.js", "Prisma")),
        ),
        process = listOf(
            ProcessStep("内容梳理", "先明确要展示的人、事、作品和联系方式。", listOf("明确重点", "整理资料", "确认顺序")),
            ProcessStep("信息归类", "把作品、文章、能力和联系入口放到固定位置。", listOf("作品归档", "文章整理", "入口统一")),
            ProcessStep("体验呈现", "用克制的布局让内容更容易浏览。", listOf("层次清晰", "弱网可读", "状态自然")),
            ProcessStep("持续更新", "保持内容常新，让访问者快速了解近况。", listOf("补充新作", "更新文章", "检查链接")),
        ),
    )

    private suspend fun getShell(): SiteShell = coroutineScope {
        val siteRequest = async { api.get("site").dataObject() }
        val profileRequest = async { api.get("profile").dataObject() }
        val site = siteRequest.await()
        val profile = profileRequest.await()
        SiteShell(
            site = SiteInfo(site.text("site_name"), site.text("footer_text")),
            profile = Profile(
                name = profile.text("name"),
                role = profile.text("role"),
                location = profile.text("location"),
                headline = profile.text("headline"),
                summary = profile.text("summary"),
                availability = profile.text("availability"),
            ),
            socialLinks = parseSocialLinks(profile.optJSONArray("social_links")),
        )
    }

    private fun parseBanners(array: JSONArray?): List<Banner> = array.objects().map {
        Banner(
            title = it.text("title"),
            subtitle = it.text("subtitle"),
            description = it.text("description"),
            image = it.text("mobile_image", it.text("image")),
            buttonText = it.text("button_text"),
            buttonLink = it.text("button_link"),
        )
    }

    private fun parseProjects(array: JSONArray?): List<Project> = array.objects().map(::parseProject)

    private fun parseProject(item: JSONObject) = Project(
        title = item.text("title"),
        slug = item.text("slug"),
        description = item.text("description"),
        tags = item.optJSONArray("tags").strings(),
        status = item.text("stage", statusLabel(item.optInt("status", -1))),
        impact = item.text("impact"),
        category = item.text("category"),
        cover = item.text("cover"),
        role = item.text("role"),
        period = item.text("period"),
        repositoryUrl = item.text("repository_url"),
        demoUrl = item.text("demo_url", item.text("href")),
        content = htmlToText(item.text("content")),
    )

    private fun parsePosts(array: JSONArray?): List<BlogPost> = array.objects().map(::parsePost)

    private fun parsePost(item: JSONObject): BlogPost {
        val date = formatDate(item.text("publish_date"))
        return BlogPost(
            title = item.text("title"),
            slug = item.text("slug"),
            description = item.text("description"),
            tags = item.text("tags").split(Regex("[,，、\\s]+")).filter(String::isNotBlank),
            status = if (item.optBoolean("is_pinned")) "精选" else date,
            category = item.text("category"),
            coverImage = item.text("cover_image"),
            content = htmlToText(item.text("content")),
            source = item.text("source"),
            publishedAt = date,
            authorName = item.optJSONObject("author")?.text("name").orEmpty(),
        )
    }

    private fun parseSocialLinks(array: JSONArray?): List<SocialLink> = array.objects()
        .filter { it.optInt("status", 1) == 1 }
        .sortedBy { it.optInt("sort_order") }
        .map { SocialLink(it.text("label"), it.text("href"), it.text("hint")) }

    private fun JSONObject.dataObject(): JSONObject = optJSONObject("data") ?: JSONObject()
    private fun JSONObject.dataArray(): JSONArray = optJSONArray("data") ?: JSONArray()
    private fun JSONObject.text(key: String, fallback: String = ""): String =
        optString(key).takeUnless { it == "null" || it.isBlank() } ?: fallback

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).mapNotNull(::optJSONObject)

    private fun JSONArray?.strings(): List<String> =
        if (this == null) emptyList() else (0 until length()).mapNotNull { optString(it).takeIf(String::isNotBlank) }

    private fun pageQuery() = mapOf("page" to "1", "page_size" to "10")
    private fun encodePath(value: String) = java.net.URLEncoder.encode(value, Charsets.UTF_8.name())
    private fun statusLabel(status: Int) = when (status) { 0 -> "草稿"; 1 -> "已发布"; 2 -> "已关闭"; else -> "" }

    private fun formatDate(value: String): String {
        if (value.isBlank()) return ""
        return runCatching {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            SimpleDateFormat("yyyy.MM.dd", Locale.CHINA).format(checkNotNull(parser.parse(value)))
        }.getOrDefault(value.take(10).replace('-', '.'))
    }

    private fun htmlToText(value: String): String = value
        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</(h[1-6]|li|p)>", RegexOption.IGNORE_CASE), "\n\n")
        .replace(Regex("<li[^>]*>", RegexOption.IGNORE_CASE), "• ")
        .replace(Regex("<[^>]+>"), "")
        .replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<")
        .replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'")
        .trim()
}
