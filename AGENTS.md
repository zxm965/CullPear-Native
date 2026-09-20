# CullPear 研发规范

本文件适用于整个仓库。实现需求前先确认改动所属职责，避免把网络、状态、业务 UI 和应用装配混在同一文件中。

## 技术与兼容性

- 使用 Kotlin、Jetpack Compose 与 Material 3，不新增 XML 页面布局。
- Android 配置保持 `compileSdk 36`、`targetSdk 36`、`minSdk 26`；调整这些版本前必须验证依赖与 GitHub Actions SDK 可用性。
- Java/Kotlin 字节码兼容级别保持 Java 11，CI 使用项目现有 Gradle Wrapper 和 JDK 25。
- 优先使用 AndroidX 与平台能力；引入新依赖前确认确有必要，并统一登记到 `gradle/libs.versions.toml`。

## 目录与职责

```text
app/src/main/kotlin/com/zxm965/cullpear/
├── app/                 # 应用装配、依赖容器、导航、全局弹窗
├── core/
│   ├── data/            # Repository 与数据转换
│   ├── designsystem/    # 主题、颜色、设计令牌
│   ├── model/           # 领域模型与通用状态
│   ├── network/         # HTTP 与远程数据读取
│   ├── preferences/     # 本地偏好
│   ├── update/          # 版本检查、下载与安装流程
│   └── ui/              # 通用 UI 状态和基类
├── feature/             # 按 home、works、blogs 等业务功能分区
└── ui/components/       # 跨功能复用的 Compose 组件
tests/unit/              # JVM 单元测试
docs/                    # 维护与发布文档、品牌源文件
```

- 类名使用 PascalCase，函数和变量使用 camelCase，常量使用 UPPER_SNAKE_CASE。
- Screen/Route 只负责展示与交互编排；网络读取和数据转换不得直接散落在 Composable 中。
- 可复用组件放入 `ui/components`，只服务单一业务的组件留在对应 `feature`。
- 用户可见文案默认使用简体中文，避免在多个页面复制同一状态判断。

## 应用内更新

- 使用公开的 GitHub Releases Atom 源，不使用匿名 GitHub REST API，避免触发每小时频率限制。
- 启动自动检查最多每 6 小时一次；设置页允许用户手动强制检查。
- 标准流程为：发现版本后弹窗说明 → 用户确认下载 → DownloadManager 后台下载 → 完成后弹窗提示安装。
- 不把 GitHub Token、签名密码或其他凭证写入源码、资源、日志或 APK。
- Android 普通应用不能静默安装；必须保留系统的“安装未知应用”和安装确认步骤。
- APK 文件名固定为 `CullPear-{version}.apk`，必须与发布工作流和下载地址生成规则一致。

## 图标与视觉资源

- 品牌图标源文件为 `docs/branding/cupear.svg`，Android 图标由该文件转换为 VectorDrawable。
- 自适应图标背景使用纯白色，前景当前缩放为 `0.50`，确保不同启动器蒙版下保留足够白边。
- 同时维护普通、圆形和 monochrome 图标；修改后至少执行一次资源编译。

## 测试与验证

- JVM 测试统一放在仓库根目录 `tests/unit`，不要重新创建模板 `app/src/test` 或 `androidTest` 目录。
- 纯版本比较、Release 源解析和格式化逻辑必须有单元测试。
- 常规改动至少执行：

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

- 发布相关改动还必须验证 `lintRelease bundleRelease assembleRelease`、APK/AAB 签名和 SHA-256 文件。

## Git 与发布边界

- `master` 同步到 Gitee `origin` 和 GitHub `github`；不得重写共享历史。
- 未经当前任务明确要求，不自行提交或推送代码。
- **不得自行创建、移动或删除版本标签，不得触发发布工作流，不得创建、修改或删除 GitHub/Gitee Release。** 过去的发布授权不延续到后续任务；每次发布都必须获得用户在当前任务中的明确指令。
- 版本标签遵循 `vMAJOR.MINOR.PATCH`，同一标签在两个远程必须指向同一提交。
- `release/`、keystore、密码文件和 `dist/` 只保留在本地忽略目录；发布证书必须持续使用同一份并由用户自行备份。

## 文档同步

- 架构、构建命令、更新源或发布流程发生变化时，同步更新 `README.md` 与 `docs/RELEASING.md`。
- 规范发生变化时同步更新本文件，避免实现与文档不一致。
