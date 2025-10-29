# 悬浮窗权限按钮说明

## 功能概述

已添加悬浮窗权限申请按钮，方便用户快速授予悬浮窗权限。

## 实现内容

### 1. 布局文件修改 (`app/src/main/res/layout/main.xml`)

在无障碍服务按钮下方添加了悬浮窗权限按钮：

```xml
<!-- 悬浮窗权限按钮 -->
<Button
    android:id="@+id/btnOverlayPermission"
    android:layout_width="match_parent"
    android:layout_height="56dp"
    android:text="悬浮窗权限"
    android:textSize="18sp"
    android:textStyle="bold"
    android:textColor="@color/white"
    android:background="@drawable/button_secondary"
    android:layout_marginBottom="12dp"
    android:elevation="4dp" />
```

### 2. MainActivity 修改

#### 添加按钮变量
```java
private Button btnOverlayPermission;
```

#### 初始化按钮
```java
btnOverlayPermission = findViewById(R.id.btnOverlayPermission);
```

#### 添加点击事件
```java
btnOverlayPermission.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // 检查悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
            // 跳转到权限设置页面
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this, "悬浮窗权限已授予", Toast.LENGTH_SHORT).show();
        }
    }
});
```

### 3. 权限声明 (`AndroidManifest.xml`)

已声明悬浮窗权限：

```xml
<!-- 系统窗口权限，用于悬浮窗 -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

## 使用说明

### 按钮位置
悬浮窗权限按钮位于无障碍服务按钮下方，启动脚本按钮上方。

### 功能
1. **检查权限状态**：点击按钮后会检查悬浮窗权限是否已授予
2. **已授予**：显示提示"悬浮窗权限已授予"
3. **未授予**：自动跳转到系统权限设置页面，让用户授予权限

### 按钮文字
- 默认文字："悬浮窗权限"
- 可以修改为更适合的文案，如"开启悬浮窗"、"悬浮窗设置"等

## 权限说明

### 为什么需要悬浮窗权限？

悬浮窗权限（`SYSTEM_ALERT_WINDOW`）允许应用在其他应用的上层显示内容。在自动化脚本中，这个权限用于：

1. **防止应用被系统杀死**：显示悬浮窗可以让系统认为应用正在使用中
2. **用户交互**：悬浮窗可以显示脚本状态，并提供暂停/继续功能
3. **保持后台运行**：特别是在 MIUI 等系统上，有助于避免被后台管理清理

### 如何授予权限？

1. **方法一**：点击新添加的"悬浮窗权限"按钮
2. **方法二**：在启动脚本时，系统会自动提示申请权限

### 系统设置路径

不同设备的设置路径可能不同：
- **原生 Android**：设置 → 应用 → 特殊权限 → 显示在其他应用上层
- **MIUI**：设置 → 应用设置 → 权限管理 → 悬浮窗
- **ColorOS**：设置 → 应用管理 → 权限 → 悬浮窗
- **其他系统**：路径可能略有不同

## 兼容性

- **Android 6.0+ (API 23+)**：需要手动授予悬浮窗权限
- **Android 5.1 及以下**：自动授予（无需用户操作）

## 注意事项

1. **不可跳过**：悬浮窗权限对于自动化脚本的正常运行至关重要
2. **手动授予**：必须在系统设置中手动授予，无法通过代码自动授予
3. **应用重启**：授予权限后可能需要重启应用才能生效
4. **部分设备**：部分厂商系统（如 MIUI）可能需要在"应用管理"中额外开启后台运行权限

## 测试建议

在测试时请验证：
1. ✅ 按钮能正常显示
2. ✅ 点击按钮能跳转到设置页面
3. ✅ 授予权限后，按钮显示"权限已授予"提示
4. ✅ 悬浮窗服务能正常启动
5. ✅ 应用切换到后台仍能正常运行

