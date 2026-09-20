# 测试目录

测试代码统一放在仓库根目录，避免与产品代码混杂。

- `unit/`：本地 JVM 单元测试，由 Gradle 的 `test` 与 `testDebugUnitTest` 任务执行。
- 需要真机或模拟器的 Compose UI 测试暂不启用；确有测试需求时再建立 `ui/` 并配置 `androidTest` source set。

单元测试文件应保持与被测代码相同的包名，例如：

```text
tests/unit/com/zxm965/cullpear/core/data/ContentRepositoryTest.kt
```
