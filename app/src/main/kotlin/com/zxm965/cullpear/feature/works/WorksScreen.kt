package com.zxm965.cullpear.feature.works

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zxm965.cullpear.core.data.ContentRepository
import com.zxm965.cullpear.core.model.Project
import com.zxm965.cullpear.core.ui.LoadingViewModel
import com.zxm965.cullpear.ui.components.ContentCard
import com.zxm965.cullpear.ui.components.ContentList
import com.zxm965.cullpear.ui.components.PageHeader
import com.zxm965.cullpear.ui.components.RemoteImage
import com.zxm965.cullpear.ui.components.StatefulScreen
import com.zxm965.cullpear.ui.components.TagRow

class WorksViewModel(private val repository: ContentRepository) : LoadingViewModel<List<Project>>() {
    init { refresh() }
    fun refresh() = load(repository::getProjects)
    class Factory(private val repository: ContentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = WorksViewModel(repository) as T
    }
}

@Composable
fun WorksRoute(repository: ContentRepository, onProject: (String) -> Unit) {
    val model: WorksViewModel = viewModel(factory = WorksViewModel.Factory(repository))
    StatefulScreen(model.state.value, model::refresh) { projects ->
        ContentList {
            item {
                PageHeader(
                    eyebrow = "Works",
                    title = "作品",
                    description = "把项目当作一组正在生长的产品样本，而不是静态作品集。",
                    tags = projects.flatMap(Project::tags).distinct().take(9),
                )
            }
            if (projects.isEmpty()) item { Text("暂无可展示的作品。") }
            itemsIndexed(projects, key = { _, it -> it.slug.ifBlank { it.title } }) { index, project ->
                ContentCard {
                    RemoteImage(project.cover, project.title, Modifier.fillMaxWidth().height(190.dp))
                    Text((index + 1).toString().padStart(2, '0'), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(project.title, fontWeight = FontWeight.Bold)
                            Text(listOf(project.role, project.period).filter(String::isNotBlank).joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(project.status, color = MaterialTheme.colorScheme.primary)
                    }
                    if (project.impact.isNotBlank()) Text(project.impact, color = MaterialTheme.colorScheme.secondary)
                    TagRow(project.tags)
                    if (project.slug.isNotBlank()) OutlinedButton(onClick = { onProject(project.slug) }) { Text("查看详情") }
                }
            }
        }
    }
}
