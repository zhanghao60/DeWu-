# Android 12 兼容性分析报告

## 📱 项目当前配置

- **最小 SDK**: 24 (Android 7.0)
- **目标 SDK**: 31 (Android 12)
- **编译 SDK**: 34 (Android 14) - 满足依赖库要求
- **支持 Android 12**: ✅ 是（API 31/32）

> **重要说明**：`compileSdk` 和 `targetSdk` 是不同的概念：
> - `compileSdk = 34`：允许使用 API 34 的 API，但不会改变应用行为
> - `targetSdk = 31`：应用针对 Android 12 运行时行为优化

## ⚠️ 发现的 Android 12 兼容性问题

### 1. 前台服务类型未声明 (严重)

**问题**：从 Android 12 (API 31) 开始，所有前台服务必须在 AndroidManifest.xml 中明确声明服务类型。

**当前状态**：`FloatingWindowService` 没有声明 `foregroundServiceType`

**影响**：
- Android 12+ 设备上可能导致 `ForegroundServiceStartNotAllowedException`
- 服务可能被系统强制停止
- 悬浮窗功能无法正常工作

**解决方案**：

在 `AndroidManifest.xml` 中添加：

```xml
<!-- 悬浮窗服务 -->
<service
    android:name=".FloatingWindowService"
    android:exported="false"
    android:foregroundServiceType="specialUse"
    android:permission="android.permission.BIND_FOREGROUND_SERVICE">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="自动化脚本保持运行" />
</service>
```

或者更简单的方式，在 AndroidManifest.xml 添加：

```xml
<!-- 悬浮窗服务 -->
<service
    android:name=".FloatingWindowService"
    android:exported="false">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="automation_script" />
</service>
```

### 2. 服务启动方式需要调整 (重要)

**问题**：Android 12 对后台服务有更严格的限制。当前使用 `context.startService()` 可能在某些情况下失效。

**当前代码** (`FloatingWindowService.java:176-180`):
```java
public static void startService(Context context, String status) {
    Intent intent = new Intent(context, FloatingWindowService.class);
    intent.putExtra("status", status);
    context.startService(intent);
}
```

**解决方案**：

修改为使用 `startForegroundService()` 并在服务中调用 `startForeground()`：

```java
public static void startService(Context context, String status) {
    Intent intent = new Intent(context, FloatingWindowService.class);
    intent.putExtra("status", status);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent);
    } else {
        context.startService(intent);
    }
}
```

并在 `FloatingWindowService.onCreate()` 中添加：

```java
@Override
public void onCreate() {
    super.onCreate();
    instance = this;
    isPaused = false;
    
    // Android 12+ 需要启动前台服务
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel channel = new NotificationChannel(
            "floating_window_channel",
            "悬浮窗服务",
            NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
        
        Notification notification = new NotificationCompat.Builder(this, "floating_window_channel")
            .setContentTitle("脚本运行中")
            .setContentText("自动执行中...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build();
        
        startForeground(1, notification);
    }
    
    Log.d(TAG, "FloatingWindowService created");
    createFloatingWindow();
}
```

### 3. 目标 SDK 设置错误 (重要)

**问题**：`targetSdk = 36` 指向 Android 16（尚未发布），可能是笔误。

**建议**：
- Android 12: 设置 `targetSdk = 31` 或 `targetSdk = 32`
- 如需支持最新特性，可逐步升级到 API 33+

```kotlin
defaultConfig {
    applicationId = "com.zh.dewuautomationscript"
    minSdk = 24
    targetSdk = 31  // 或 32 (Android 12)
    versionCode = 1
    versionName = "1.0"
}
```

## ✅ Android 12 上兼容的功能

以下功能在 Android 12 上**无需修改**，已经兼容：

1. **无障碍服务** - `AccessibilityService` 正常工作
2. **悬浮窗权限** - `SYSTEM_ALERT_WINDOW` 权限依然有效
3. **MediaProjection 截屏** - 截屏 API 正常工作
4. **网络请求** - OkHttp 正常工作
5. **QUERY_ALL_PACKAGES** - 已在 AndroidManifest 中声明

## 📋 推荐的修改步骤

### 步骤 1: 修复 FloatingWindowService

修改 `app/src/main/java/com/zh/dewuautomationscript/FloatingWindowService.java`：

```java
@Override
public void onCreate() {
    super.onCreate();
    instance = this;
    isPaused = false;
    
    // Android 12+ 前台服务支持
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            NotificationChannel channel = new NotificationChannel(
                "floating_window_channel",
                "悬浮窗服务",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            
            Notification notification = new NotificationCompat.Builder(this, "floating_window_channel")
                .setContentTitle("脚本运行中")
                .setContentText("正在执行自动化任务...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();
            
            startForeground(1, notification);
            Log.d(TAG, "前台服务已启动");
        } catch (Exception e) {
            Log.e(TAG, "启动前台服务失败", e);
        }
    }
    
    Log.d(TAG, "FloatingWindowService created");
    createFloatingWindow();
}

public static void startService(Context context, String status) {
    Intent intent = new Intent(context, FloatingWindowService.class);
    intent.putExtra("status", status);
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            context.startForegroundService(intent);
        } catch (IllegalStateException e) {
            Log.e(TAG, "启动前台服务失败: " + e.getMessage());
            // 降级到普通启动
            context.startService(intent);
        }
    } else {
        context.startService(intent);
    }
}
```

### 步骤 2: 修改 AndroidManifest.xml

```xml
<!-- 悬浮窗服务 -->
<service
    android:name=".FloatingWindowService"
    android:exported="false"
    android:enabled="true">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="automation_script_keep_alive" />
</service>
```

### 步骤 3: 修改 build.gradle.kts

```kotlin
compileSdk {
    version = release(34)  // Android 14 - 满足依赖库要求
}

defaultConfig {
    applicationId = "com.zh.dewuautomationscript"
    minSdk = 24
    targetSdk = 31  // Android 12 - 保持应用行为不变
    versionCode = 1
    versionName = "1.0"
}
```

> **为什么 compileSdk 和 targetSdk 不同？**
> - `compileSdk = 34`：满足 AndroidX 库（如 appcompat 1.6.1）的最低要求
> - `targetSdk = 31`：确保应用在 Android 12 上的行为不变
> - 这是推荐做法：用最新 SDK 编译，但针对特定版本优化

### 步骤 4: 添加必需的依赖

在 `app/build.gradle.kts` 中确保有：

```kotlin
dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("androidx.core:core-ktx:1.8.0")  // 用于通知兼容性
    // ... 其他依赖
}
```

## 🧪 测试建议

在 Android 12 设备上测试以下场景：

1. ✅ 启动应用
2. ✅ 启动无障碍服务
3. ✅ 启动悬浮窗服务
4. ✅ 启动脚本执行
5. ✅ 切换到后台，检查服务是否继续运行
6. ✅ 设备锁屏，检查服务是否继续运行
7. ✅ 重启设备后，检查应用是否正常启动

## ⚠️ 已知限制

Android 12 的以下变化可能会影响用户体验：

1. **应用休眠**：用户长期不使用应用后，系统会自动暂停应用
   - **解决方案**：引导用户关闭电池优化
   
2. **后台限制增强**：系统更严格地限制后台活动
   - **解决方案**：使用前台服务（已实现）
   
3. **权限设置更严格**：某些权限需要用户在设置中手动授权
   - **解决方案**：提供详细的权限设置指南

## 📝 总结

**当前状态**：项目**基本兼容** Android 12，但需要修复前台服务相关的问题。

**必须修复的问题**：
1. ✅ 添加前台服务通知
2. ✅ 使用 `startForegroundService()` 
3. ✅ 修正 `targetSdk` 版本号

**优先级**：高 - 建议在发布前修复

