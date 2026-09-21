package com.zxm965.cullpear.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zxm965.cullpear.R
import com.zxm965.cullpear.core.model.UiState
import com.zxm965.cullpear.core.network.ImageLoader
import com.zxm965.cullpear.core.network.RemoteImageLoader

@Composable
fun <T> StatefulScreen(
    state: UiState<T>,
    onRetry: () -> Unit,
    isRefreshing: Boolean = false,
    loading: @Composable () -> Unit = { ScreenLoadingSkeleton() },
    content: @Composable (T) -> Unit,
) {
    when (state) {
        UiState.Loading -> loading()
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
        is UiState.Success -> {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRetry,
                modifier = Modifier.fillMaxSize(),
            ) {
                content(state.value)
            }
        }
    }
}

@Composable
fun ScreenLoadingSkeleton(
    imageHeight: Dp? = null,
    showPageHeader: Boolean = true,
    showLeadCard: Boolean = false,
    showSectionTitle: Boolean = true,
) {
    val brush = rememberSkeletonBrush()
    ContentList {
        if (showPageHeader) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SkeletonBlock(brush, Modifier.fillMaxWidth(0.22f).height(14.dp))
                    SkeletonBlock(brush, Modifier.fillMaxWidth(0.48f).height(36.dp))
                    SkeletonBlock(brush, Modifier.fillMaxWidth().height(20.dp))
                    SkeletonBlock(brush, Modifier.fillMaxWidth(0.78f).height(20.dp))
                }
            }
        }
        if (showLeadCard) {
            item {
                ContentCard {
                    SkeletonBlock(brush, Modifier.fillMaxWidth(0.28f).height(16.dp))
                    SkeletonBlock(brush, Modifier.fillMaxWidth(0.72f).height(40.dp))
                    SkeletonBlock(brush, Modifier.fillMaxWidth().height(22.dp))
                    SkeletonBlock(brush, Modifier.fillMaxWidth(0.84f).height(22.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SkeletonBlock(brush, Modifier.width(84.dp).height(36.dp))
                        SkeletonBlock(brush, Modifier.width(112.dp).height(36.dp))
                    }
                    SkeletonBlock(brush, Modifier.fillMaxWidth(0.88f).height(42.dp))
                }
            }
        }
        if (showSectionTitle) {
            item { SkeletonBlock(brush, Modifier.fillMaxWidth(0.3f).height(26.dp)) }
        }
        items(2) {
            ContentCard {
                imageHeight?.let { SkeletonBlock(brush, Modifier.fillMaxWidth().height(it)) }
                SkeletonBlock(brush, Modifier.fillMaxWidth(0.62f).height(24.dp))
                SkeletonBlock(brush, Modifier.fillMaxWidth().height(18.dp))
                SkeletonBlock(brush, Modifier.fillMaxWidth(0.76f).height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBlock(brush, Modifier.width(68.dp).height(30.dp))
                    SkeletonBlock(brush, Modifier.width(82.dp).height(30.dp))
                    SkeletonBlock(brush, Modifier.width(74.dp).height(30.dp))
                }
            }
        }
    }
}

@Composable
private fun rememberSkeletonBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val offset by transition.animateFloat(
        initialValue = -SKELETON_TRAVEL_PX,
        targetValue = SKELETON_TRAVEL_PX,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SKELETON_DURATION_MS),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeleton-offset",
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(offset - SKELETON_BAND_PX, 0f),
        end = Offset(offset, SKELETON_BAND_PX),
    )
}

@Composable
private fun SkeletonBlock(brush: Brush, modifier: Modifier) {
    Spacer(modifier = modifier.background(brush, RoundedCornerShape(8.dp)))
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

private sealed interface RemoteImageState {
    data object Loading : RemoteImageState
    data class Success(val bitmap: Bitmap) : RemoteImageState
    data object Error : RemoteImageState
}

@Composable
fun RemoteImage(
    url: String,
    description: String,
    modifier: Modifier,
    imageLoader: ImageLoader = RemoteImageLoader,
) {
    key(url, imageLoader) {
        RemoteImageContent(url, description, modifier, imageLoader)
    }
}

@Composable
private fun RemoteImageContent(
    url: String,
    description: String,
    modifier: Modifier,
    imageLoader: ImageLoader,
) {
    var preview by remember { mutableStateOf(false) }
    var state by remember {
        mutableStateOf<RemoteImageState>(if (url.isBlank()) RemoteImageState.Error else RemoteImageState.Loading)
    }

    LaunchedEffect(url, imageLoader) {
        if (url.isBlank()) {
            state = RemoteImageState.Error
        } else {
            state = RemoteImageState.Loading
            state = imageLoader.load(url).fold(
                onSuccess = RemoteImageState::Success,
                onFailure = { RemoteImageState.Error },
            )
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(8.dp))) {
        Crossfade(
            targetState = state,
            modifier = Modifier.fillMaxSize(),
            animationSpec = tween(durationMillis = IMAGE_FADE_DURATION_MS),
            label = "remote-image",
        ) { imageState ->
            when (imageState) {
                RemoteImageState.Loading -> ImageSkeleton(loading = true)
                RemoteImageState.Error -> ImageSkeleton(loading = false)
                is RemoteImageState.Success -> {
                    Image(
                        bitmap = imageState.bitmap.asImageBitmap(),
                        contentDescription = description,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clickable { preview = true },
                    )
                }
            }
        }
    }

    val bitmap = (state as? RemoteImageState.Success)?.bitmap
    if (preview && bitmap != null) {
        Dialog(onDismissRequest = { preview = false }) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = description,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().clickable { preview = false },
            )
        }
    }
}

@Composable
private fun ImageSkeleton(loading: Boolean) {
    if (loading) {
        Box(modifier = Modifier.fillMaxSize().background(rememberSkeletonBrush()))
    } else {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ms_rounded_broken_image_24),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

private const val IMAGE_FADE_DURATION_MS = 220
private const val SKELETON_DURATION_MS = 1_100
private const val SKELETON_TRAVEL_PX = 1_200f
private const val SKELETON_BAND_PX = 320f

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
