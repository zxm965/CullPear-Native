package com.zxm965.cullpear.app

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zxm965.cullpear.BuildConfig
import com.zxm965.cullpear.core.update.UpdateState
import com.zxm965.cullpear.feature.about.AboutRoute
import com.zxm965.cullpear.feature.blogs.BlogDetailRoute
import com.zxm965.cullpear.feature.blogs.BlogsRoute
import com.zxm965.cullpear.feature.contact.ContactRoute
import com.zxm965.cullpear.feature.home.HomeRoute
import com.zxm965.cullpear.feature.settings.SettingsRoute
import com.zxm965.cullpear.feature.works.WorkDetailRoute
import com.zxm965.cullpear.feature.works.WorksRoute

private data class TabDestination(val route: String, val label: String, val symbol: String)

private val tabs = listOf(
    TabDestination("home", "首页", "⌂"),
    TabDestination("works", "作品", "▣"),
    TabDestination("about", "能力", "◇"),
    TabDestination("blogs", "博客", "≡"),
    TabDestination("contact", "联系", "✉"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CullPearApp(container: AppContainer) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route.orEmpty()
    val isSettings = route == "settings"
    val isDetail = route.startsWith("works/") || route.startsWith("blogs/")
    val title = when {
        route == "home" -> "拾梨"
        route.startsWith("works") -> if (isDetail) "作品详情" else "作品"
        route == "about" -> "能力"
        route.startsWith("blogs") -> if (isDetail) "文章详情" else "博客"
        route == "contact" -> "联系"
        route == "settings" -> "设置"
        else -> "拾梨"
    }

    LaunchedEffect(Unit) {
        container.updateManager.checkForUpdate()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (isSettings || isDetail) {
                        TextButton(onClick = { navController.popBackStack() }) { Text("‹ 返回") }
                    }
                },
                actions = {
                    if (!isSettings) TextButton(onClick = { navController.navigate("settings") }) { Text("设置") }
                },
            )
        },
        bottomBar = {
            if (!isSettings) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = entry?.destination?.hierarchy?.any { destination -> destination.route == tab.route } == true ||
                            (isDetail && route.startsWith(tab.route))
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(tab.symbol) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController = navController, startDestination = "home", modifier = Modifier.padding(padding)) {
            composable("home") {
                HomeRoute(container.repository) { navController.navigate("works/${Uri.encode(it)}") }
            }
            composable("works") {
                WorksRoute(container.repository) { navController.navigate("works/${Uri.encode(it)}") }
            }
            composable(
                route = "works/{slug}",
                arguments = listOf(navArgument("slug") { type = NavType.StringType }),
            ) { WorkDetailRoute(container.repository, it.arguments?.getString("slug").orEmpty()) }
            composable("about") { AboutRoute(container.repository) }
            composable("blogs") {
                BlogsRoute(container.repository) { navController.navigate("blogs/${Uri.encode(it)}") }
            }
            composable(
                route = "blogs/{slug}",
                arguments = listOf(navArgument("slug") { type = NavType.StringType }),
            ) { BlogDetailRoute(container.repository, it.arguments?.getString("slug").orEmpty()) }
            composable("contact") { ContactRoute(container.repository) }
            composable("settings") { SettingsRoute(container.themePreferences, container.updateManager) }
        }
    }

    UpdatePromptHost(container)
}

@Composable
private fun UpdatePromptHost(container: AppContainer) {
    val updateManager = container.updateManager
    val updateState = updateManager.state
    var postponedDownloadVersion by rememberSaveable { mutableStateOf<String?>(null) }
    var postponedInstallVersion by rememberSaveable { mutableStateOf<String?>(null) }

    when (updateState) {
        is UpdateState.Available -> {
            val update = updateState.update
            if (postponedDownloadVersion != update.version) {
                AlertDialog(
                    onDismissRequest = { postponedDownloadVersion = update.version },
                    title = { Text("发现新版本 v${update.version}") },
                    text = {
                        Column(
                            modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text("当前版本 ${BuildConfig.VERSION_NAME} · 最新版本 ${update.version}")
                            Text(update.releaseNotes)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = updateManager::downloadAvailableUpdate) {
                            Text("立即下载")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { postponedDownloadVersion = update.version }) {
                            Text("稍后")
                        }
                    },
                )
            }
        }

        is UpdateState.ReadyToInstall -> {
            if (postponedInstallVersion != updateState.version) {
                AlertDialog(
                    onDismissRequest = { postponedInstallVersion = updateState.version },
                    title = { Text("更新已下载") },
                    text = { Text("CullPear v${updateState.version} 已准备好，安装后即可使用新版本。") },
                    confirmButton = {
                        TextButton(onClick = updateManager::installDownloadedUpdate) {
                            Text("立即安装")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { postponedInstallVersion = updateState.version }) {
                            Text("稍后")
                        }
                    },
                )
            }
        }

        is UpdateState.PermissionRequired -> {
            if (postponedInstallVersion != updateState.version) {
                AlertDialog(
                    onDismissRequest = { postponedInstallVersion = updateState.version },
                    title = { Text("允许安装更新") },
                    text = { Text("请允许 CullPear 安装未知来源应用，返回后点击继续安装。") },
                    confirmButton = {
                        TextButton(onClick = updateManager::installDownloadedUpdate) {
                            Text("继续安装")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { postponedInstallVersion = updateState.version }) {
                            Text("稍后")
                        }
                    },
                )
            }
        }

        else -> Unit
    }
}
