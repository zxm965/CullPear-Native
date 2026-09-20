package com.zxm965.cullpear.feature.blogs

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zxm965.cullpear.core.data.ContentRepository
import com.zxm965.cullpear.core.model.BlogPost
import com.zxm965.cullpear.core.ui.LoadingViewModel
import com.zxm965.cullpear.ui.components.ContentCard
import com.zxm965.cullpear.ui.components.ContentList
import com.zxm965.cullpear.ui.components.DetailBody
import com.zxm965.cullpear.ui.components.RemoteImage
import com.zxm965.cullpear.ui.components.Section
import com.zxm965.cullpear.ui.components.StatefulScreen
import com.zxm965.cullpear.ui.components.TagRow

class BlogDetailViewModel(private val repository: ContentRepository, private val slug: String) : LoadingViewModel<BlogPost?>() {
    init { refresh() }
    fun refresh() = load { repository.getPost(slug) }
    class Factory(private val repository: ContentRepository, private val slug: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = BlogDetailViewModel(repository, slug) as T
    }
}

@Composable
fun BlogDetailRoute(repository: ContentRepository, slug: String) {
    val model: BlogDetailViewModel = viewModel(key = "blog-$slug", factory = BlogDetailViewModel.Factory(repository, slug))
    StatefulScreen(model.state.value, model::refresh) { post ->
        if (post == null) {
            Text("暂时没有找到这篇文章。")
            return@StatefulScreen
        }
        ContentList {
            item {
                ContentCard {
                    RemoteImage(post.coverImage, post.title, Modifier.fillMaxWidth().height(230.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(post.status, color = MaterialTheme.colorScheme.primary)
                        Text(post.source, color = MaterialTheme.colorScheme.secondary)
                    }
                    Text(post.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    val meta = listOf(post.authorName, post.publishedAt, post.category).filter(String::isNotBlank).joinToString(" · ")
                    if (meta.isNotBlank()) Text(meta, color = MaterialTheme.colorScheme.secondary)
                    Text(post.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TagRow(post.tags)
                }
            }
            item { Section("正文") { DetailBody(post.content.ifBlank { post.description }) } }
        }
    }
}
