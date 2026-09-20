package com.zxm965.cullpear.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zxm965.cullpear.core.data.ContentRepository
import com.zxm965.cullpear.core.model.HomeContent
import com.zxm965.cullpear.core.model.Project
import com.zxm965.cullpear.core.ui.LoadingViewModel
import com.zxm965.cullpear.ui.components.ContentCard
import com.zxm965.cullpear.ui.components.ContentList
import com.zxm965.cullpear.ui.components.ExternalLinkButton
import com.zxm965.cullpear.ui.components.RemoteImage
import com.zxm965.cullpear.ui.components.Section
import com.zxm965.cullpear.ui.components.StatefulScreen
import com.zxm965.cullpear.ui.components.TagRow

class HomeViewModel(private val repository: ContentRepository) : LoadingViewModel<HomeContent>() {
    init { refresh() }
    fun refresh() = load(repository::getHome)

    class Factory(private val repository: ContentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(repository) as T
    }
}

@Composable
fun HomeRoute(repository: ContentRepository, onProject: (String) -> Unit) {
    val model: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
    StatefulScreen(model.state.value, model::refresh) { HomeScreen(it, onProject) }
}

@Composable
private fun HomeScreen(content: HomeContent, onProject: (String) -> Unit) {
    ContentList {
        item {
            ContentCard {
                val profile = content.shell.profile
                Text(profile.role.uppercase(), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                Text(profile.headline.ifBlank { content.shell.site.name }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(profile.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TagRow(listOf(profile.name, profile.location).filter(String::isNotBlank))
                if (profile.availability.isNotBlank()) {
                    Text("● ${profile.availability}", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        if (content.banners.isNotEmpty()) {
            item { Text("动态", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            items(content.banners, key = { it.title + it.image }) { banner ->
                ContentCard {
                    RemoteImage(banner.image, banner.title, Modifier.fillMaxWidth().height(170.dp))
                    if (banner.subtitle.isNotBlank()) Text(banner.subtitle, color = MaterialTheme.colorScheme.primary)
                    Text(banner.title, fontWeight = FontWeight.Bold)
                    if (banner.description.isNotBlank()) Text(banner.description)
                    ExternalLinkButton(banner.buttonText.ifBlank { "了解更多" }, banner.buttonLink)
                }
            }
        }
        item { Text("精选作品", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        if (content.projects.isEmpty()) item { Text("暂无可展示的作品。") }
        items(content.projects, key = { it.slug.ifBlank { it.title } }) { project ->
            ProjectCard(project, onProject)
        }
        if (content.shell.socialLinks.isNotEmpty()) {
            item {
                Section("链接") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        content.shell.socialLinks.forEach { link ->
                            ContentCard {
                                Text(link.label, fontWeight = FontWeight.Bold)
                                if (link.hint.isNotBlank()) Text(link.hint, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                ExternalLinkButton("打开", link.href)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(project: Project, onProject: (String) -> Unit) {
    ContentCard {
        RemoteImage(project.cover, project.title, Modifier.fillMaxWidth().height(185.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(project.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
            if (project.status.isNotBlank()) Text(project.status, color = MaterialTheme.colorScheme.primary)
        }
        if (project.impact.isNotBlank()) Text(project.impact, color = MaterialTheme.colorScheme.secondary)
        TagRow(project.tags.take(4))
        if (project.slug.isNotBlank()) {
            androidx.compose.material3.OutlinedButton(onClick = { onProject(project.slug) }) { Text("查看详情") }
        }
    }
}
