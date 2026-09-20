# Android 发布流程

项目使用 GitHub Actions 完成持续验证和标签发布：

- `Android CI`：`master` 更新、Pull Request 或手动触发时运行单元测试、Lint 和 Debug APK 构建。
- `Android Release`：收到 `vX.Y.Z` 标签后构建签名 APK/AAB、校验签名、生成 SHA-256 文件并创建 GitHub Release。

## 一次性配置

### 1. 创建个人分发签名密钥

即使 APK 只给自己使用，Android 仍要求每个安装包签名，而且升级版本必须一直使用同一证书。项目提供一次性生成工具：

```bash
./scripts/generate-release-keystore.sh
```

工具会在已忽略的 `release/` 目录生成：

- `cullpear-self-release.jks`：固定的个人发布证书。
- `github-secrets.env`：需要录入 GitHub 的四项 Secret。

请离线备份整个 `release/` 目录。密钥不得提交到 Git；密钥丢失后，新版本将无法覆盖安装旧版本。

### 2. 配置 GitHub Environment 与 Secrets

在 GitHub 仓库进入 **Settings > Environments**，创建 `release` 环境。建议为该环境开启人工审批，然后配置以下 Secrets：

| Secret | 含义 |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | keystore 的 Base64 单行内容 |
| `ANDROID_KEYSTORE_PASSWORD` | keystore 密码 |
| `ANDROID_KEY_ALIAS` | 密钥别名 |
| `ANDROID_KEY_PASSWORD` | 密钥密码 |

逐行复制 `release/github-secrets.env` 中等号后的值到对应 GitHub Secret。Base64 只是传输编码，不是加密；必须只保存到 GitHub Secrets。

## 日常发布

1. 确保 `master` 的 CI 已通过。
2. 按语义化版本创建标签并推送：

```bash
git switch master
git pull --ff-only
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0
git push github v1.0.0
```

项目本地同时配置 Gitee `origin` 和 GitHub `github` 远程。标签推送到两端可以避免镜像延迟；不要重新创建指向不同提交的同名标签。

GitHub 收到标签后会自动：

1. 从标签得到 `versionName`，并使用 Actions run number 生成递增 `versionCode`。
2. 执行测试与 Release Lint。
3. 恢复临时 keystore，生成并验证签名 APK/AAB。
4. 生成 SHA-256 校验文件。
5. 上传 Actions Artifact，并创建带自动发行说明的 GitHub Release。

## 本地发布构建

本地签名构建使用与 CI 相同的环境变量：

```bash
export VERSION_NAME=1.0.0
export VERSION_CODE=10001
export ANDROID_KEYSTORE_PATH="$PWD/release/cullpear-self-release.jks"
# 其余三个值读取 release/github-secrets.env 后设置
./gradlew clean lintRelease bundleRelease assembleRelease
```

产物位置：

```text
app/build/outputs/apk/release/app-release.apk
app/build/outputs/bundle/release/app-release.aab
```

## Google Play

GitHub Release 适合分发 APK；Google Play 应上传 AAB。首次发布时启用 Play App Signing，并使用单独的上传密钥签署后续 AAB。

## 应用内更新

应用启动时会读取 `zxm965/CullPear-Native` 的公开 Release Atom 源，自动检查最多每 6 小时一次，设置页仍可手动强制检查。发现更高版本后会弹窗展示版本号和发行说明；用户确认后使用 Android DownloadManager 后台下载 APK，下载完成后再次弹窗提示安装。该方案不调用有匿名频率限制的 GitHub REST API。

用于分发更新的 GitHub 仓库及其 Release 必须允许未登录访问。私有仓库的 Release API 和 APK 下载都需要身份凭证，不能把 GitHub Token 内置到 APK 中，否则任何人都可以从安装包提取 Token。若源代码需要保持私有，请改用单独的公开发行仓库，并同步调整 `AppUpdateManager` 中的 Release API 地址。

Android 不允许普通应用静默安装，因此首次更新需要在系统页面允许“安装未知应用”，每次安装仍可能需要手动确认。第一次从 Debug 版本切换到 Release 版本时签名不同，需要先卸载 Debug 版；之后使用同一发布密钥即可直接覆盖更新。
