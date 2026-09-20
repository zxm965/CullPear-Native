package com.zxm965.cullpear.core.model

data class SiteInfo(val name: String = "", val footerText: String = "")

data class Profile(
    val name: String = "",
    val role: String = "",
    val location: String = "",
    val headline: String = "",
    val summary: String = "",
    val availability: String = "",
)

data class SocialLink(val label: String, val href: String, val hint: String = "")

data class Banner(
    val title: String,
    val subtitle: String = "",
    val description: String = "",
    val image: String = "",
    val buttonText: String = "",
    val buttonLink: String = "",
)

data class Project(
    val title: String,
    val slug: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val status: String = "",
    val impact: String = "",
    val category: String = "",
    val cover: String = "",
    val role: String = "",
    val period: String = "",
    val repositoryUrl: String = "",
    val demoUrl: String = "",
    val content: String = "",
)

data class BlogPost(
    val title: String,
    val slug: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val status: String = "",
    val category: String = "",
    val coverImage: String = "",
    val content: String = "",
    val source: String = "",
    val publishedAt: String = "",
    val authorName: String = "",
)

data class ContactInfo(
    val name: String = "",
    val role: String = "",
    val email: String = "",
    val phone: String = "",
    val wechat: String = "",
    val address: String = "",
    val location: String = "",
    val availability: String = "",
    val resumeUrl: String = "",
    val businessHours: String = "",
    val mapUrl: String = "",
)

data class SiteShell(
    val site: SiteInfo = SiteInfo(),
    val profile: Profile = Profile(),
    val socialLinks: List<SocialLink> = emptyList(),
)

data class HomeContent(
    val shell: SiteShell,
    val banners: List<Banner>,
    val projects: List<Project>,
)

data class ContactContent(
    val shell: SiteShell,
    val contact: ContactInfo,
    val socialLinks: List<SocialLink>,
)

data class StackGroup(val title: String, val description: String, val tools: List<String>)
data class ProcessStep(val title: String, val description: String, val checkpoints: List<String>)

data class AboutContent(
    val stack: List<String>,
    val groups: List<StackGroup>,
    val process: List<ProcessStep>,
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val value: T, val warning: String = "") : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
