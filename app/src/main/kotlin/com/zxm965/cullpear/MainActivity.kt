package com.zxm965.cullpear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zxm965.cullpear.app.AppContainer
import com.zxm965.cullpear.app.CullPearApp
import com.zxm965.cullpear.core.designsystem.theme.CullPearTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = AppContainer(applicationContext)
        setContent {
            CullPearTheme(
                mode = container.themePreferences.mode,
                accent = container.themePreferences.accent,
            ) {
                CullPearApp(container)
            }
        }
    }
}
