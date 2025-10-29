# 依赖库编译 SDK 版本修复

## 问题描述

编译时出现 17 个 AAR 元数据检查错误，原因是：
- 项目的 `compileSdk = 31`（Android 12）
- 但依赖的 AndroidX 库需要 `compileSdk >= 33` 或 `34`
  - androidx.appcompat:appcompat:1.6.1 需要 ≥ 33
  - androidx.activity:activity:1.8.0 需要 ≥ 34
  - androidx.emoji2 需要 ≥ 32
  - 等等...

## 解决方案

### 核心概念

**重要**：`compileSdk` 和 `targetSdk` 是不同的概念！

| 参数 | 作用 | 值 |
|------|------|-----|
| `compileSdk` | 决定可以用哪些 API | 34 (Android 14) |
| `targetSdk` | 决定应用的运行时行为 | 31 (Android 12) |
| `minSdk` | 决定最低支持的设备 | 24 (Android 7.0) |

### 修复方法

修改 `app/build.gradle.kts`：

```kotlin
android {
    compileSdk {
        version = release(34)  // Android 14 - 满足依赖要求
    }

    defaultConfig {
        minSdk = 24
        targetSdk = 31  // Android 12 - 保持应用行为不变
        // ...
    }
}
```

### 为什么这样设置？

1. **compileSdk = 34**
   - 满足所有 AndroidX 依赖库的最低要求
   - 可以使用最新的 API（虽然不建议直接使用）
   - **不影响**应用行为

2. **targetSdk = 31**
   - 针对 Android 12 优化
   - 保持应用的运行时行为不变
   - 确保 Android 12 特性正常工作

3. **minSdk = 24**
   - 最低支持 Android 7.0
   - 覆盖大部分设备

## 影响

### ✅ 解决的问题
- 消除所有 17 个 AAR 元数据警告
- 项目可以正常编译
- 所有依赖库正常工作

### ✅ 不变的部分
- 应用行为：仍然针对 Android 12 优化
- 目标设备：仍然支持 Android 7.0+
- 功能：所有功能保持不变

### 📱 兼容性
- **Android 7.0 - 13**：完全支持
- **Android 12**：完全支持（targetSdk = 31）
- **Android 14+**：向前兼容（compileSdk = 34）

## 最佳实践

这是 Google 推荐的配置方式：

```
compileSdk >= targetSdk >= minSdk
    34          31          24
```

- **compileSdk**：用最新 SDK 编译，获得最新 API 和 bug 修复
- **targetSdk**：根据你的主要用户群设置
- **minSdk**：根据功能需求设置最低支持版本

## 常见误区

❌ **错误理解**：
- "compileSdk = 34 意味着应用只能在 Android 14 上运行"
- "targetSdk = 31 意味着应用不能在 Android 13 上运行"

✅ **正确理解**：
- compileSdk 只影响**编译时**，不限制运行时
- targetSdk 是**目标**行为，不是限制
- 应用可以在所有支持 minSdk 的设备上运行

## 验证

修复后，编译时应该不再有 AAR 元数据警告。运行：

```bash
./gradlew clean build
```

应该看到：
- ✅ 17 个警告全部消失
- ✅ 编译成功
- ✅ 应用行为不变

