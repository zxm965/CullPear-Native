package com.zxm965.cullpear.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zxm965.cullpear.core.model.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun <T> StatefulScreen(
    state: UiState<T>,
    onRetry: () -> Unit,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = onRetry) { Text("重试") }
            }
        }
        is UiState.Success -> content(state.value)
    }
}

@Composable
fun ContentList(content: LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
fun PageHeader(eyebrow: String, title: String, description: String = "", tags: List<String> = emptyList()) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            eyebrow.uppercase(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (description.isNotBlank()) {
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TagRow(tags)
    }
}

@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
fun TagRow(tags: List<String>, modifier: Modifier = Modifier) {
    if (tags.isEmpty()) return
    FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            Text(
                tag,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(7.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
fun ContentCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun RemoteImage(url: String, description: String, modifier: Modifier = Modifier) {
    if (url.isBlank()) return
    var preview by remember { mutableStateOf(false) }
    val bitmap by produceState<android.graphics.Bitmap?>(null, url) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val connection = URL(url).openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 10_000
                    connection.inputStream.use(BitmapFactory::decodeStream)
                } finally {
                    connection.disconnect()
                }
            }.getOrNull()
        }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = description,
            contentScale = ContentScale.Crop,
            modifier = modifier.clickable { preview = true },
        )
        if (preview) {
            Dialog(onDismissRequest = { preview = false }) {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = description,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().clickable { preview = false },
                )
            }
        }
    }
}

@Composable
fun ExternalLinkButton(label: String, url: String, modifier: Modifier = Modifier) {
    if (url.isBlank()) return
    val uriHandler = LocalUriHandler.current
    var error by remember { mutableStateOf("") }
    OutlinedButton(
        onClick = {
            error = runCatching { uriHandler.openUri(url) }.exceptionOrNull()?.message.orEmpty()
        },
        modifier = modifier,
    ) { Text(label) }
    if (error.isNotBlank()) Text("无法打开链接", color = MaterialTheme.colorScheme.error)
}

@Composable
fun InfoRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(label, modifier = Modifier.weight(0.28f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, modifier = Modifier.weight(0.72f), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun DetailBody(text: String) {
    val paragraphs = remember(text) { text.split(Regex("\\n{2,}")).filter(String::isNotBlank) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        paragraphs.forEach { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
