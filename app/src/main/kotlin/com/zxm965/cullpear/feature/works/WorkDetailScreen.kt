package com.zxm965.cullpear.feature.works

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zxm965.cullpear.core.data.ContentRepository
import com.zxm965.cullpear.core.model.Project
import com.zxm965.cullpear.core.ui.LoadingViewModel
import com.zxm965.cullpear.ui.components.ContentCard
import com.zxm965.cullpear.ui.components.ContentList
import com.zxm965.cullpear.ui.components.DetailBody
import com.zxm965.cullpear.ui.components.ExternalLinkButton
import com.zxm965.cullpear.ui.components.InfoRow
import com.zxm965.cullpear.ui.components.RemoteImage
import com.zxm965.cullpear.ui.components.ScreenLoadingSkeleton
import com.zxm965.cullpear.ui.components.Section
import com.zxm965.cullpear.ui.components.StatefulScreen
import com.zxm965.cullpear.ui.components.TagRow

class WorkDetailViewModel(private val repository: ContentRepository, private val slug: String) : LoadingViewModel<Project?>() {
    init { refresh() }
    fun refresh() = load { repository.getProject(slug) }
    class Factory(private val repository: ContentRepository, private val slug: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = WorkDetailViewModel(repository, slug) as T
    }
}

@Composable
fun WorkDetailRoute(repository: ContentRepository, slug: String) {
    val model: WorkDetailViewModel = viewModel(key = "work-$slug", factory = WorkDetailViewModel.Factory(repository, slug))
    StatefulScreen(
        state = model.state.value,
        onRetry = model::refresh,
        isRefreshing = model.isRefreshing.value,
        loading = {
            ScreenLoadingSkeleton(
                imageHeight = 230.dp,
                showPageHeader = false,
                showSectionTitle = false,
            )
        },
    ) { project ->
        if (project == null) {
            Text("暂时没有找到这个作品。")
            return@StatefulScreen
        }
        ContentList {
            item {
                ContentCard {
                    RemoteImage(project.cover, project.title, Modifier.fillMaxWidth().height(230.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(project.category.uppercase(), color = MaterialTheme.colorScheme.secondary)
                        Text(project.status, color = MaterialTheme.colorScheme.primary)
                    }
                    Text(project.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(project.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TagRow(project.tags)
                }
            }
            item {
                Section("项目信息") {
                    ContentCard {
                        InfoRow("角色", project.role)
                        InfoRow("周期", project.period)
                        InfoRow("亮点", project.impact)
                    }
                }
            }
            if (project.content.isNotBlank()) item { Section("详情") { DetailBody(project.content) } }
            item {
                Section("相关链接") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExternalLinkButton("在线预览", project.demoUrl, Modifier.weight(1f))
                        ExternalLinkButton("代码仓库", project.repositoryUrl, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
