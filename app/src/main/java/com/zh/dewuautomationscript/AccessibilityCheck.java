package com.zh.dewuautomationscript;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * 无障碍服务检查类
 */
public class AccessibilityCheck {
    
    private static final String TAG = "AccessibilityCheck";
    
    /**
     * 检查无障碍服务是否已启用
     * @param context 上下文
     * @return 是否已启用
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        return AutomationAccessibilityService.isServiceRunning();
    }
    
    /**
     * 打开无障碍设置页面
     * @param context 上下文
     */
    public static void openAccessibilitySettings(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            Log.d(TAG, "已打开无障碍设置页面");
        } catch (Exception e) {
            Log.e(TAG, "打开无障碍设置失败", e);
        }
    }
    
    /**
     * 直接跳转到应用的无障碍服务设置页面
     * @param context 上下文
     */
    public static void openAppAccessibilitySettings(Context context) {
        try {
            String serviceName = context.getPackageName() + "/" + 
                AutomationAccessibilityService.class.getName();
            Log.d(TAG, "服务名称: " + serviceName);
            
            boolean success = false;
            
            // 方法1: Android 10+ (API 29+) 使用 AccessibilityDetailsSettings
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                try {
                    // 尝试使用 ACTION_ACCESSIBILITY_DETAILS_SETTINGS (Android 10+)
                    Intent intent = new Intent();
                    intent.setAction("android.settings.ACCESSIBILITY_DETAILS_SETTINGS");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("android.intent.extra.COMPONENT_NAME", serviceName);
                    
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                        Log.d(TAG, "成功使用 ACTION_ACCESSIBILITY_DETAILS_SETTINGS (Android 10+)");
                        success = true;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "ACTION_ACCESSIBILITY_DETAILS_SETTINGS 失败: " + e.getMessage());
                }
            }
            
            // 方法2: Android 11+ (API 30+) 使用 packageName 参数
            if (!success && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                try {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("packageName", context.getPackageName());
                    
                    context.startActivity(intent);
                    Log.d(TAG, "成功使用 Android 11+ 的 packageName 参数");
                    success = true;
                } catch (Exception e) {
                    Log.d(TAG, "Android 11+ packageName 参数失败: " + e.getMessage());
                }
            }
            
            // 方法3: 尝试使用服务名称参数
            if (!success) {
                try {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("service_name", serviceName);
                    
                    context.startActivity(intent);
                    Log.d(TAG, "成功使用 service_name 参数");
                    success = true;
                } catch (Exception e) {
                    Log.d(TAG, "service_name 参数失败: " + e.getMessage());
                }
            }
            
            // 如果成功，显示提示信息
            if (success) {
                Toast.makeText(context, "请在页面中找到「得物自动化脚本」并开启", Toast.LENGTH_LONG).show();
            } else {
                Log.e(TAG, "所有直接跳转方法失败，无法打开指定应用的无障碍设置");
                Toast.makeText(context, "无法打开无障碍设置页面", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "打开应用无障碍服务设置失败", e);
            Toast.makeText(context, "无法打开无障碍设置页面", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 尝试使用不同的方法跳转到应用的无障碍服务设置
     * 这个方法针对不同厂商的设备使用了多种策略
     * @param context 上下文
     */
    public static void openAppAccessibilitySettingsAdvanced(Context context) {
        String serviceName = context.getPackageName() + "/" + 
            AutomationAccessibilityService.class.getName();
        
        String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();
        Log.d(TAG, "设备厂商: " + manufacturer);
        
        boolean success = false;
        
        // 首先尝试使用标准方法来直接跳转到应用的无障碍设置
        if (!success) {
            success = tryOpenAccessibilityDetailsSettings(context, serviceName);
        }
        
        // 方法1: 针对小米（MIUI）的特殊处理
        if (!success && (manufacturer.contains("xiaomi") || manufacturer.contains("redmi"))) {
            try {
                // 小米设备尝试使用特定的 Intent
                Intent intent1 = new Intent("miui.intent.action.ACCESSIBILITY_SETTINGS");
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.putExtra("service_name", serviceName);
                if (intent1.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent1);
                    Log.d(TAG, "小米设备: 使用MIUI特定的Intent");
                    success = true;
                }
            } catch (Exception e) {
                Log.d(TAG, "小米特定Intent失败: " + e.getMessage());
            }
        }
        
        // 方法2: 对于 OPPO/OnePlus
        if (!success && (manufacturer.contains("oppo") || manufacturer.contains("oneplus") || manufacturer.contains("realme"))) {
            try {
                Intent intent2 = new Intent("oppo.settings.accessibility");
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra("service_name", serviceName);
                if (intent2.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent2);
                    Log.d(TAG, "OPPO设备: 使用OPPO特定的Intent");
                    success = true;
                }
            } catch (Exception e) {
                Log.d(TAG, "OPPO特定Intent失败: " + e.getMessage());
            }
        }
        
        // 方法3: 对于华为
        if (!success && (manufacturer.contains("huawei") || manufacturer.contains("honor"))) {
            try {
                Intent intent3 = new Intent("huawei.settings.ACCESSIBILITY_SETTINGS");
                intent3.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent3.putExtra("service_name", serviceName);
                if (intent3.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent3);
                    Log.d(TAG, "华为设备: 使用华为特定的Intent");
                    success = true;
                }
            } catch (Exception e) {
                Log.d(TAG, "华为特定Intent失败: " + e.getMessage());
            }
        }
        
        // 方法4: 使用标准方法直接跳转到应用设置
        if (!success) {
            success = openAppAccessibilitySettingsDirect(context, serviceName);
        }
        
        if (success) {
            // 成功跳转后，显示提示
            Toast.makeText(context, "请在页面中找到「得物自动化脚本」并开启", Toast.LENGTH_LONG).show();
        } else {
            // 所有方法都失败
            Log.e(TAG, "所有方法都失败，无法打开指定应用的无障碍设置");
            Toast.makeText(context, "无法打开无障碍设置页面", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 尝试使用 ACTION_ACCESSIBILITY_DETAILS_SETTINGS 打开无障碍详情设置
     * @param context 上下文
     * @param serviceName 服务名称
     * @return 是否成功
     */
    private static boolean tryOpenAccessibilityDetailsSettings(Context context, String serviceName) {
        try {
            Intent intent = new Intent();
            intent.setAction("android.settings.ACCESSIBILITY_DETAILS_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("android.intent.extra.COMPONENT_NAME", serviceName);
            
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                Log.d(TAG, "成功使用 ACTION_ACCESSIBILITY_DETAILS_SETTINGS");
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "ACTION_ACCESSIBILITY_DETAILS_SETTINGS 失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 尝试直接打开应用的无障碍服务设置
     * @param context 上下文
     * @param serviceName 服务名称
     * @return 是否成功
     */
    private static boolean openAppAccessibilitySettingsDirect(Context context, String serviceName) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("service_name", serviceName);
            intent.putExtra("component_name", serviceName);
            
            context.startActivity(intent);
            Log.d(TAG, "成功使用 service_name 和 component_name 参数");
            return true;
        } catch (Exception e) {
            Log.d(TAG, "直接设置失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查并请求无障碍权限
     * @param context 上下文
     * @return 是否已启用无障碍服务
     */
    public static boolean checkAndRequestAccessibilityPermission(Context context) {
        if (isAccessibilityServiceEnabled(context)) {
            Log.d(TAG, "无障碍服务已启用");
            return true;
        } else {
            Log.d(TAG, "无障碍服务未启用，正在打开设置页面");
            openAppAccessibilitySettings(context);
            return false;
        }
    }
    
    /**
     * 获取无障碍服务状态描述
     * @param context 上下文
     * @return 状态描述
     */
    public static String getAccessibilityStatusText(Context context) {
        if (isAccessibilityServiceEnabled(context)) {
            return "无障碍服务已启用";
        } else {
            return "无障碍服务未启用，请到设置中开启";
        }
    }
}
