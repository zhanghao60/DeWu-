package com.zh.dewuautomationscript;

import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * MIUI系统兼容性辅助工具
 */
public class MiuiCompatibilityHelper {
    
    private static final String TAG = "MiuiCompatibilityHelper";
    
    /**
     * 检查是否为MIUI系统
     */
    public static boolean isMiui() {
        try {
            return Build.MANUFACTURER.toLowerCase().contains("xiaomi") || 
                   Build.MANUFACTURER.toLowerCase().contains("redmi");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查MIUI版本
     */
    public static String getMiuiVersion() {
        try {
            if (isMiui()) {
                return Build.VERSION.INCREMENTAL;
            }
        } catch (Exception e) {
            Log.e(TAG, "获取MIUI版本失败", e);
        }
        return "Unknown";
    }
    
    /**
     * 检查是否为MIUI 12或更高版本
     */
    public static boolean isMiui12OrHigher() {
        try {
            if (!isMiui()) {
                return false;
            }
            
            String version = getMiuiVersion();
            if (version.equals("Unknown")) {
                return false;
            }
            
            // MIUI版本号格式通常是 V12.5.1.0.RJACNXM
            if (version.startsWith("V12") || version.startsWith("V13") || version.startsWith("V14")) {
                return true;
            }
            
            return false;
        } catch (Exception e) {
            Log.e(TAG, "检查MIUI版本失败", e);
            return false;
        }
    }
    
    /**
     * 检查MIUI系统上的特殊权限
     */
    public static boolean checkMiuiPermissions(Context context) {
        try {
            if (!isMiui()) {
                return true; // 非MIUI系统
            }
            
            // MIUI系统需要检查的特殊权限
            Log.d(TAG, "检查MIUI系统权限...");
            
            // 检查无障碍服务权限
            if (!AccessibilityCheck.isAccessibilityServiceEnabled(context)) {
                Log.w(TAG, "MIUI系统：无障碍服务未启用");
                return false;
            }
            
            // MIUI 12+ 需要额外检查
            if (isMiui12OrHigher()) {
                Log.d(TAG, "MIUI 12+ 系统，需要检查额外权限");
                // 这里可以添加更多MIUI 12+的特殊检查
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "检查MIUI权限失败", e);
            return false;
        }
    }
    
    /**
     * 获取MIUI系统信息
     */
    public static String getMiuiInfo() {
        StringBuilder info = new StringBuilder();
        info.append("设备制造商: ").append(Build.MANUFACTURER).append("\n");
        info.append("设备型号: ").append(Build.MODEL).append("\n");
        info.append("Android版本: ").append(Build.VERSION.RELEASE).append("\n");
        info.append("API级别: ").append(Build.VERSION.SDK_INT).append("\n");
        
        if (isMiui()) {
            info.append("MIUI版本: ").append(getMiuiVersion()).append("\n");
            info.append("MIUI 12+: ").append(isMiui12OrHigher() ? "是" : "否");
        } else {
            info.append("非MIUI系统");
        }
        
        return info.toString();
    }
    
    /**
     * 检查无障碍服务在MIUI上的兼容性
     */
    public static boolean checkAccessibilityCompatibility(Context context) {
        try {
            if (!isMiui()) {
                return true; // 非MIUI系统，假设兼容
            }
            
            // MIUI系统上的特殊检查
            if (isMiui12OrHigher()) {
                Log.d(TAG, "检测到MIUI 12+，无障碍服务可能需要额外权限");
                return true; // 可能需要用户手动授权
            }
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "检查无障碍兼容性失败", e);
            return false;
        }
    }
    
    /**
     * 获取MIUI系统建议
     */
    public static String getMiuiSuggestions() {
        if (!isMiui()) {
            return "非MIUI系统，无需特殊设置";
        }
        
        StringBuilder suggestions = new StringBuilder();
        suggestions.append("MIUI系统使用建议:\n");
        suggestions.append("1. 确保在设置中开启无障碍服务\n");
        suggestions.append("2. 关闭MIUI优化（如果影响功能）\n");
        suggestions.append("3. 允许应用后台运行\n");
        suggestions.append("4. 关闭省电模式\n");
        suggestions.append("5. 允许应用自启动");
        
        return suggestions.toString();
    }
}
