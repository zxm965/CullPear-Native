package com.zxm965.cullpear.feature.blogs

import androidx.compose.foundation.layout.Arrangement
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
import com.zxm965.cullpear.core.model.BlogPost
import com.zxm965.cullpear.core.ui.LoadingViewModel
import com.zxm965.cullpear.ui.components.ContentCard
import com.zxm965.cullpear.ui.components.ContentList
import com.zxm965.cullpear.ui.components.PageHeader
import com.zxm965.cullpear.ui.components.RemoteImage
import com.zxm965.cullpear.ui.components.StatefulScreen
import com.zxm965.cullpear.ui.components.TagRow

class BlogsViewModel(private val repository: ContentRepository) : LoadingViewModel<List<BlogPost>>() {
    init { refresh() }
    fun refresh() = load(repository::getPosts)
    class Factory(private val repository: ContentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = BlogsViewModel(repository) as T
    }
}

@Composable
fun BlogsRoute(repository: ContentRepository, onPost: (String) -> Unit) {
    val model: BlogsViewModel = viewModel(factory = BlogsViewModel.Factory(repository))
    StatefulScreen(model.state.value, model::refresh) { posts ->
        ContentList {
            item {
                PageHeader(
                    eyebrow = "Blogs",
                    title = "文章",
                    description = "用更短的路径记录产品、工程和内容系统里的判断。",
                    tags = posts.flatMap(BlogPost::tags).distinct().take(8),
                )
            }
            posts.firstOrNull { it.status == "精选" }?.let { highlighted ->
                item {
                    ContentCard {
                        Text("精选", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(highlighted.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = { onPost(highlighted.slug) }) { Text("查看详情") }
                    }
                }
            }
            if (posts.isEmpty()) item { Text("暂无可展示的文章。") }
            itemsIndexed(posts, key = { _, it -> it.slug.ifBlank { it.title } }) { index, post ->
                ContentCard {
                    RemoteImage(post.coverImage, post.title, Modifier.fillMaxWidth().height(180.dp))
                    Text((index + 1).toString().padStart(2, '0'), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(post.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text(post.status, color = MaterialTheme.colorScheme.primary)
                    }
                    val meta = listOf(post.authorName, post.publishedAt).filter(String::isNotBlank).joinToString(" · ")
                    if (meta.isNotBlank()) Text(meta, color = MaterialTheme.colorScheme.secondary)
                    TagRow(post.tags)
                    if (post.slug.isNotBlank()) OutlinedButton(onClick = { onPost(post.slug) }) { Text("查看详情") }
                }
            }
        }
    }
}
