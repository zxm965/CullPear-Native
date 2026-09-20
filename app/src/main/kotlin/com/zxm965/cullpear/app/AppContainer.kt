package com.zxm965.cullpear.app

import android.content.Context
import com.zxm965.cullpear.core.data.ContentRepository
import com.zxm965.cullpear.core.network.ContentApiClient
import com.zxm965.cullpear.core.preferences.ThemePreferences
import com.zxm965.cullpear.core.update.AppUpdateManager

class AppContainer(context: Context) {
    val repository = ContentRepository(ContentApiClient())
    val themePreferences = ThemePreferences(context.applicationContext)
    val updateManager = AppUpdateManager(context.applicationContext)
}
