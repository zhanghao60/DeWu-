# Android 12 悬浮窗修复方案

## 问题描述

在 Android 12 设备上，悬浮窗只显示在桌面，打开其他应用时悬浮窗被覆盖，无法在其他应用上层显示。

## 问题原因

从 Android 12 (API 31) 开始，系统对前台服务的管理更加严格：

1. **前台服务类型限制**：所有前台服务必须明确声明服务类型
2. **悬浮窗权限更严格**：需要正确的前台服务类型才能在其他应用上层显示
3. **服务启动限制**：必须在 5 秒内调用 `startForeground()` 并指定服务类型

## 解决方案

### 1. AndroidManifest.xml 修改

在悬浮窗服务声明中添加 `foregroundServiceType="specialUse"`：

```xml
<!-- 悬浮窗服务 -->
<service
    android:name=".FloatingWindowService"
    android:exported="false"
    android:enabled="true"
    android:foregroundServiceType="specialUse">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="automation_script_keep_alive" />
</service>
```

**关键属性**：
- `android:foregroundServiceType="specialUse"`：声明为特殊用途前台服务
- `android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE`：指定特殊用途的描述

### 2. FloatingWindowService.java 修改

在启动前台服务时指定服务类型：

```java
// Android 12+ 需要指定前台服务类型
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    startForeground(1, notification, 
        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
} else {
    startForeground(1, notification);
}
```

## 修改内容

### 已修改的文件

1. **AndroidManifest.xml**
   - 添加 `android:foregroundServiceType="specialUse"`

2. **FloatingWindowService.java**
   - 在 `startForeground()` 中添加服务类型参数
   - 针对 Android 12+ 使用 `FOREGROUND_SERVICE_TYPE_SPECIAL_USE`

## 验证方法

修复后，请按以下步骤验证：

1. **重新编译应用**
   ```bash
   ./gradlew clean build
   ```

2. **安装应用到设备**
   - 确保是 Android 12 设备
   - 授予所有必要权限（无障碍服务、悬浮窗权限）

3. **启动悬浮窗服务**
   - 点击"启动脚本"按钮
   - 查看悬浮窗是否出现

4. **测试悬浮窗显示**
   - 打开其他应用（如浏览器、微信等）
   - 检查悬浮窗是否仍然显示在应用上层
   - 如果显示正常，说明修复成功

## 常见问题

### Q1: 悬浮窗还是不显示在其他应用上层

**可能原因**：
1. 权限未完全授予
2. 系统优化导致服务被杀死

**解决方法**：
- 检查悬浮窗权限是否已授予
- 关闭省电模式
- 在系统设置中设置应用为"无限制"

### Q2: 前台服务启动失败

**错误信息**：`ForegroundServiceStartNotAllowedException`

**原因**：没有在 5 秒内调用 `startForeground()`

**解决方法**：
- 确保在 `onCreate()` 中立即调用 `startForeground()`
- 不要在其他线程或延迟中调用

### Q3: MIUI 等定制系统仍然有问题

**原因**：MIUI 等系统有额外的后台管理机制

**解决方法**：
1. 关闭 MIUI 优化
2. 设置应用为无限制运行
3. 关闭省电模式
4. 锁定应用（在最近任务中下拉锁定）

## 前台服务类型说明

### specialUse

`FOREGROUND_SERVICE_TYPE_SPECIAL_USE` 用于：
- 不符合其他标准前台服务类型的特殊情况
- 需要持续运行但不符合其他类型的服务
- 悬浮窗服务通常使用此类型

### 其他常用类型

- `dataSync`：数据同步
- `location`：位置服务
- `mediaPlayback`：媒体播放
- `phoneCall`：电话呼叫

## 重要提示

1. **权限是前提**：必须先授予悬浮窗权限（`SYSTEM_ALERT_WINDOW`）
2. **前台服务必须**：在 Android 12+ 上必须作为前台服务运行
3. **及时调用**：必须在服务启动后立即调用 `startForeground()`
4. **系统差异**：不同厂商的系统（MIUI、ColorOS 等）可能有额外限制

## 参考文档

- [Android 12 前台服务限制](https://developer.android.com/about/versions/12/foreground-services)
- [悬浮窗权限设置](https://developer.android.com/reference/android/provider/Settings#ACTION_MANAGE_OVERLAY_PERMISSION)

