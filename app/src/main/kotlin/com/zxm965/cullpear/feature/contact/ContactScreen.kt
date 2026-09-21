package com.zxm965.cullpear.feature.contact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zxm965.cullpear.core.data.ContentRepository
import com.zxm965.cullpear.core.model.ContactContent
import com.zxm965.cullpear.core.ui.LoadingViewModel
import com.zxm965.cullpear.ui.components.ContentCard
import com.zxm965.cullpear.ui.components.ContentList
import com.zxm965.cullpear.ui.components.ExternalLinkButton
import com.zxm965.cullpear.ui.components.InfoRow
import com.zxm965.cullpear.ui.components.PageHeader
import com.zxm965.cullpear.ui.components.ScreenLoadingSkeleton
import com.zxm965.cullpear.ui.components.StatefulScreen

class ContactViewModel(private val repository: ContentRepository) : LoadingViewModel<ContactContent>() {
    init { refresh() }
    fun refresh() = load(repository::getContact)
    class Factory(private val repository: ContentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ContactViewModel(repository) as T
    }
}

@Composable
fun ContactRoute(repository: ContentRepository) {
    val model: ContactViewModel = viewModel(factory = ContactViewModel.Factory(repository))
    StatefulScreen(
        state = model.state.value,
        onRetry = model::refresh,
        isRefreshing = model.isRefreshing.value,
        loading = { ScreenLoadingSkeleton() },
    ) { content ->
        val contact = content.contact
        ContentList {
            item {
                PageHeader(
                    eyebrow = "Contact",
                    title = contact.name.ifBlank { "联系" },
                    description = "把身份、联系方式和协作偏好整理成一个轻量入口。",
                    tags = listOf(contact.location, contact.businessHours, contact.role).filter(String::isNotBlank),
                )
            }
            item {
                ContentCard {
                    Text(contact.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(contact.role, color = MaterialTheme.colorScheme.primary)
                    Text(contact.location, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                ContentCard {
                    InfoRow("邮箱", contact.email)
                    InfoRow("电话", contact.phone)
                    InfoRow("微信", contact.wechat)
                    InfoRow("地址", contact.address)
                    InfoRow("协作地点", contact.location)
                    InfoRow("服务时间", contact.businessHours)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExternalLinkButton("发送邮件", contact.email.takeIf(String::isNotBlank)?.let { "mailto:$it" }.orEmpty())
                        ExternalLinkButton("拨打电话", contact.phone.takeIf(String::isNotBlank)?.let { "tel:${it.filter { ch -> ch.isDigit() || ch == '+' }}" }.orEmpty())
                        ExternalLinkButton("查看简历", contact.resumeUrl)
                        ExternalLinkButton("查看地图", contact.mapUrl)
                    }
                }
            }
            if (content.socialLinks.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("社交链接", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        content.socialLinks.forEach { link ->
                            ContentCard {
                                Text(link.label, fontWeight = FontWeight.Bold)
                                if (link.hint.isNotBlank()) Text(link.hint)
                                ExternalLinkButton("打开", link.href)
                            }
                        }
                    }
                }
            }
        }
    }
}
