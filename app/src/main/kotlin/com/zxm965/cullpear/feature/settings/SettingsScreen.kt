package com.zxm965.cullpear.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zxm965.cullpear.core.designsystem.theme.color
import com.zxm965.cullpear.core.preferences.Accent
import com.zxm965.cullpear.core.preferences.ThemeMode
import com.zxm965.cullpear.core.preferences.ThemePreferences
import com.zxm965.cullpear.ui.components.ContentCard
import com.zxm965.cullpear.ui.components.ContentList
import com.zxm965.cullpear.ui.components.InfoRow
import com.zxm965.cullpear.ui.components.PageHeader

@Composable
fun SettingsRoute(preferences: ThemePreferences) {
    ContentList {
        item { PageHeader("偏好", "设置") }
        item {
            Text("外观模式", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemeMode.entries.forEach { mode ->
                    SelectButton(
                        label = when (mode) { ThemeMode.SYSTEM -> "跟随系统"; ThemeMode.LIGHT -> "浅色"; ThemeMode.DARK -> "深色" },
                        selected = preferences.mode == mode,
                        onClick = { preferences.updateMode(mode) },
                    )
                }
            }
        }
        item {
            Text("主题色", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Accent.entries.forEach { accent ->
                    SelectButton(
                        label = when (accent) { Accent.PEAR -> "青梨"; Accent.OCEAN -> "海蓝"; Accent.BERRY -> "莓红"; Accent.AMBER -> "琥珀" },
                        selected = preferences.accent == accent,
                        selectedColor = accent.color(),
                        onClick = { preferences.updateAccent(accent) },
                    )
                }
            }
        }
        item {
            ContentCard {
                InfoRow("外观", preferences.mode.name)
                InfoRow("主题色", preferences.accent.name)
            }
        }
    }
}

@Composable
private fun SelectButton(label: String, selected: Boolean, selectedColor: Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) selectedColor else MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label)
            if (selected) Text("已选", color = selectedColor, fontWeight = FontWeight.Bold)
        }
    }
}
