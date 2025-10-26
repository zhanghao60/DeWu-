# 无障碍服务设置指南

## 概述

本应用已集成无障碍服务功能，用于实现自动化操作，包括截屏、点击、滑动等操作。

## 功能特性

### 1. 无障碍服务 (AutomationAccessibilityService)
- 提供屏幕截屏功能
- 支持坐标点击操作
- 支持滑动操作
- 支持按文本查找并点击元素
- 支持手势模拟

### 2. 截屏功能 (ScreenshotHelper)
- 使用MediaProjection API进行高质量截屏
- 支持保存为PNG格式
- 自动创建截图目录
- 提供截屏完成回调

### 3. 辅助工具 (AccessibilityHelper)
- 检查无障碍服务状态
- 自动打开无障碍设置页面
- 提供权限检查功能

## 权限说明

应用已声明以下权限：

```xml
<!-- 无障碍服务权限 -->
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />

<!-- 截屏权限 -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- Android 11+ 媒体权限 -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<!-- 系统窗口权限，用于悬浮窗 -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

## 使用步骤

### 1. 启用无障碍服务

1. 安装并启动应用
2. 点击"启动脚本"按钮
3. 系统会提示启用无障碍服务
4. 点击"确定"跳转到无障碍设置页面
5. 在设置中找到"得物自动化脚本"
6. 开启无障碍服务开关

### 2. 使用截屏功能

```java
// 创建截屏助手
ScreenshotHelper screenshotHelper = new ScreenshotHelper(context);

// 设置MediaProjection（需要从Activity获取）
screenshotHelper.setMediaProjection(mediaProjection);

// 执行截屏
screenshotHelper.takeScreenshot("screenshot.png", new ScreenshotHelper.ScreenshotCallback() {
    @Override
    public void onScreenshotSuccess(String filePath) {
        Log.d("Screenshot", "截屏成功: " + filePath);
    }
    
    @Override
    public void onScreenshotFailed(String error) {
        Log.e("Screenshot", "截屏失败: " + error);
    }
});
```

### 3. 使用自动化操作

```java
// 获取无障碍服务实例
AutomationAccessibilityService service = AutomationAccessibilityService.getInstance();

if (service != null) {
    // 点击坐标
    service.click(100, 200);
    
    // 滑动操作
    service.swipe(100, 200, 300, 400, 1000);
    
    // 按文本点击
    service.clickByText("确定");
    
    // 截屏
    service.takeScreenshot("auto_screenshot.png");
}
```

## 注意事项

1. **权限要求**：无障碍服务需要用户手动在设置中开启
2. **截屏限制**：截屏功能需要配合MediaProjection API使用
3. **系统兼容性**：部分功能在不同Android版本上可能有差异
4. **安全考虑**：无障碍服务具有较高权限，请谨慎使用

## 文件结构

```
app/src/main/java/com/zh/dewuautomationscript/
├── AutomationAccessibilityService.java  # 无障碍服务主类
├── AccessibilityHelper.java             # 无障碍服务辅助工具
├── ScreenshotHelper.java               # 截屏辅助工具
└── MainActivity.java                   # 主界面（已集成权限检查）

app/src/main/res/
├── xml/accessibility_service_config.xml # 无障碍服务配置
└── values/strings.xml                  # 字符串资源（包含服务描述）

app/src/main/AndroidManifest.xml        # 应用清单（包含权限和服务声明）
```

## 开发建议

1. 在实现自动化功能前，先确保无障碍服务已正确启用
2. 使用AccessibilityHelper检查服务状态
3. 截屏功能需要配合Activity的MediaProjection权限
4. 建议添加错误处理和用户提示
5. 测试时注意不同屏幕尺寸的适配

## 故障排除

### 无障碍服务无法启用
- 检查应用是否已正确安装
- 确认权限声明是否正确
- 查看系统日志中的错误信息

### 截屏功能不工作
- 确认MediaProjection权限已获取
- 检查存储权限是否已授予
- 验证文件路径是否可写

### 自动化操作无效
- 确认无障碍服务已启用
- 检查目标应用是否支持无障碍操作
- 验证坐标和文本是否正确
