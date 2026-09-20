# CullPear Android

拾梨个人内容应用的 Android 原生版本，使用 Kotlin 与 Jetpack Compose UI。功能由 `mmo-studio` 迁移而来，包括首页、作品与详情、能力、博客与详情、联系信息、外部链接、图片预览以及可持久化的外观设置。

## 技术栈

- Kotlin 2.2
- Jetpack Compose + Material 3
- Navigation Compose
- AndroidX Lifecycle / ViewModel
- 原生 `HttpURLConnection` + `org.json`，避免为简单只读 API 引入额外网络框架

## 目录职责

```text
app/src/main/kotlin/com/zxm965/cullpear/
├── app/                 # 应用装配、依赖容器、导航
├── core/
│   ├── data/            # Repository 与数据转换
│   ├── designsystem/    # 主题、颜色与设计令牌
│   ├── model/           # 领域模型、统一 UI 状态
│   ├── network/         # HTTP 客户端
│   ├── preferences/     # 本地偏好持久化
│   └── ui/              # 通用 ViewModel 基类
├── feature/             # 按业务功能分区的 Screen 与 ViewModel
│   ├── home/
│   ├── works/
│   ├── about/
│   ├── blogs/
│   ├── contact/
│   └── settings/
└── ui/components/       # 跨业务可复用 Compose 组件
tests/
└── unit/                 # 本地 JVM 单元测试
```

## 数据源

应用通过 `BuildConfig.API_BASE_URL` 访问 `https://cupear.i96.me/api/me`。如需切换环境，请修改 `app/build.gradle.kts` 中对应的 `buildConfigField`，不要在业务代码内散落地址。

## 构建

```bash
./gradlew assembleDebug
./gradlew test
```

需要 Android Studio 与项目要求的 JDK/Android SDK。

## 持续集成与发布

- `master` 与 Pull Request 会通过 GitHub Actions 自动执行测试、Lint 和 Debug 构建。
- 推送 `vX.Y.Z` 标签会自动构建签名 APK/AAB，并创建 GitHub Release。
- 应用启动时会识别公开 GitHub Release 的新版本、自动下载 APK，并调起系统安装确认。
- 完整的密钥配置、版本规则和发布步骤见 [docs/RELEASING.md](docs/RELEASING.md)。
