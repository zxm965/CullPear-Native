package com.zxm965.cullpear.app

import android.content.Context
import com.zxm965.cullpear.core.data.ContentRepository
import com.zxm965.cullpear.core.network.ContentApiClient
import com.zxm965.cullpear.core.preferences.ThemePreferences

class AppContainer(context: Context) {
    val repository = ContentRepository(ContentApiClient())
    val themePreferences = ThemePreferences(context.applicationContext)
}
