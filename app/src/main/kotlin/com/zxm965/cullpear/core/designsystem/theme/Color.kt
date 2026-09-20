package com.zxm965.cullpear.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import com.zxm965.cullpear.core.preferences.Accent

val Ink = Color(0xFF16211F)
val Paper = Color(0xFFF7F4EE)
val Night = Color(0xFF111715)
val NightSurface = Color(0xFF19211F)
val PearGreen = Color(0xFF307C67)
val OceanBlue = Color(0xFF316F9E)
val BerryRed = Color(0xFF9A5063)
val Amber = Color(0xFF9A681E)

fun Accent.color(): Color = when (this) {
    Accent.PEAR -> PearGreen
    Accent.OCEAN -> OceanBlue
    Accent.BERRY -> BerryRed
    Accent.AMBER -> Amber
}
