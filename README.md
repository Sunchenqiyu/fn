# Kotlin Ledger

一个使用 Kotlin Multiplatform 构建的全平台记账工具，提供跨平台共享的账本模型与报表逻辑，并包含 JVM 平台上的命令行应用用于快速记账与查看报表。

## 功能特性

- 📒 通用账本模型：账户、分类、交易和预算均使用 Kotlin Multiplatform 共享代码实现，可扩展到 Android、iOS、桌面或 Web。
- 💾 可插拔存储：提供内存存储与基于 JSON 文件的持久化实现，方便在不同平台复用。
- 📊 报表能力：支持按账户类型的资产负债汇总、按月份汇总分类支出与预算执行情况。
- 🛠️ 命令行工具：内置 JVM CLI，支持添加账户/分类/交易、配置预算、查看交易与生成报表。

## 目录结构

```
.
├── build.gradle.kts          // Kotlin Multiplatform + JVM CLI 构建脚本
├── settings.gradle.kts
├── src
│   ├── commonMain            // 跨平台共享模型、服务和报表逻辑
│   ├── commonTest            // 跨平台单元测试
│   ├── jvmMain               // JVM 平台 CLI 与文件存储实现
│   └── jvmTest               // JVM 平台测试
```

## 开发环境

- JDK 17+
- Gradle 8+

在当前仓库中可以直接使用系统提供的 `gradle` 命令，无需额外安装。

## 运行命令行应用

1. 构建项目：

```bash
gradle build
```

2. 运行 CLI，默认会在当前目录生成 `ledger.json`：

```bash
gradle run --args="add-account --name 现金 --type ASSET --currency CNY"
```

常用命令：

- `add-account`：新增账户，常用参数 `--name`、`--type`、`--currency`。
- `add-category`：新增分类（收入/支出/转账）。
- `add-transaction`：记账，支持 `--account`、`--amount`、`--category`、`--date`（YYYY-MM-DD）、`--notes`。
- `list-accounts`：查看账户及余额。
- `list-transactions`：查看交易记录，可配合 `--account`、`--year`、`--month` 过滤。
- `report`：生成报表，可配合 `--year`、`--month` 查看月度预算执行。
- `set-budget`：为分类设置月度预算上限。

可以使用 `--data <文件路径>` 指定账本文件位置。

## 运行测试

```bash
gradle test
```

## 扩展方向

- 集成 Compose Multiplatform 或 SwiftUI/Jetpack Compose 实现桌面与移动端界面。
- 与云端同步服务结合，实现多设备共享账本。
- 添加更多报表（现金流、资产负债趋势等）与图形化展示。

欢迎根据业务需求扩展项目！
