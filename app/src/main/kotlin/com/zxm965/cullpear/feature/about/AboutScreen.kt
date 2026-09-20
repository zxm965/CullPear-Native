package com.zxm965.cullpear.feature.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zxm965.cullpear.core.data.ContentRepository
import com.zxm965.cullpear.ui.components.ContentCard
import com.zxm965.cullpear.ui.components.ContentList
import com.zxm965.cullpear.ui.components.PageHeader
import com.zxm965.cullpear.ui.components.TagRow

@Composable
fun AboutRoute(repository: ContentRepository) {
    val content = repository.getAbout()
    ContentList {
        item {
            PageHeader(
                eyebrow = "Stack",
                title = "能力与方式",
                description = "把技术能力、协作方式和交付节奏放在同一个可扫读的页面里。",
                tags = content.stack,
            )
        }
        item { Text("能力分组", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        items(content.groups, key = { it.title }) { group ->
            ContentCard {
                Text(group.title, fontWeight = FontWeight.Bold)
                Text(group.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TagRow(group.tools)
            }
        }
        item { Text("工作流程", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        itemsIndexed(content.process, key = { _, it -> it.title }) { index, step ->
            ContentCard {
                Text((index + 1).toString().padStart(2, '0'), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(step.title, fontWeight = FontWeight.Bold)
                Text(step.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    step.checkpoints.forEach { Text("✓ $it", color = MaterialTheme.colorScheme.secondary) }
                }
            }
        }
    }
}
