package com.zh.dewuautomationscript;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 设置管理类
 * 管理用户自定义的脚本参数
 */
public class SettingsManager {
    
    private static final String PREF_NAME = "ScriptSettings";
    private static final String KEY_ENABLE_SWIPE_AFTER_CLICK = "enable_swipe_after_click";
    private static final String KEY_SWIPE_START_X = "swipe_start_x";
    private static final String KEY_SWIPE_START_Y = "swipe_start_y";
    private static final String KEY_SWIPE_END_X = "swipe_end_x";
    private static final String KEY_SWIPE_END_Y = "swipe_end_y";
    private static final String KEY_SWIPE_DURATION = "swipe_duration";
    private static final String KEY_CLICK_LOOP_COUNT = "click_loop_count"; // 控件重复点击次数
    private static final String KEY_SWIPE_WAIT_TIME = "swipe_wait_time"; // 向左滑动启动等待时间
    private static final String KEY_MAX_CONTROL_COUNT = "max_control_count"; // 最多操作控件数量
    private static final String KEY_DEWU_APP_WAIT_TIME = "dewu_app_wait_time"; // 点击启动得物app按钮后的等待时间
    private static final String KEY_CLICK_PRODUCT_LINK = "click_product_link"; // 是否点击商品链接
    private static final String KEY_ACTIVATE_CODE = "activate_code"; // 激活码
    private static final String KEY_IS_ACTIVATED = "is_activated"; // 是否已激活
    private static final String KEY_ACTIVATION_TIME = "activation_time"; // 激活时间戳
    private static final String KEY_VALID_TIME = "valid_time"; // 有效期时间
    
    // 默认值
    private static final boolean DEFAULT_ENABLE_SWIPE_AFTER_CLICK = false;
    private static final int DEFAULT_SWIPE_START_X = 800;
    private static final int DEFAULT_SWIPE_START_Y = 1200;
    private static final int DEFAULT_SWIPE_END_X = 200;
    private static final int DEFAULT_SWIPE_END_Y = 1200;
    private static final int DEFAULT_SWIPE_DURATION = 300;
    private static final int DEFAULT_CLICK_LOOP_COUNT = 5; // 5次
    private static final int DEFAULT_SWIPE_WAIT_TIME = 500; // 500毫秒
    private static final int DEFAULT_MAX_CONTROL_COUNT = 12; // 12个控件
    private static final int DEFAULT_DEWU_APP_WAIT_TIME = 3000; // 点击启动得物app按钮后等待3秒
    private static final boolean DEFAULT_CLICK_PRODUCT_LINK = false; // 默认不点击商品链接
    private static final String DEFAULT_ACTIVATE_CODE = ""; // 默认激活码为空
    private static final boolean DEFAULT_IS_ACTIVATED = false; // 默认未激活
    private static final long DEFAULT_ACTIVATION_TIME = 0; // 默认激活时间为0
    private static final String DEFAULT_VALID_TIME = "7days"; // 默认有效期7天
    
    private SharedPreferences preferences;
    
    public SettingsManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 是否启用点击后向左滑动
     */
    public boolean isSwipeAfterClickEnabled() {
        return preferences.getBoolean(KEY_ENABLE_SWIPE_AFTER_CLICK, DEFAULT_ENABLE_SWIPE_AFTER_CLICK);
    }
    
    /**
     * 设置是否启用点击后向左滑动
     */
    public void setSwipeAfterClickEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_ENABLE_SWIPE_AFTER_CLICK, enabled).apply();
    }
    
    // ==================== 滑动坐标和时间设置 ====================
    
    public int getSwipeStartX() {
        return preferences.getInt(KEY_SWIPE_START_X, DEFAULT_SWIPE_START_X);
    }
    
    public void setSwipeStartX(int x) {
        preferences.edit().putInt(KEY_SWIPE_START_X, x).apply();
    }
    
    public int getSwipeStartY() {
        return preferences.getInt(KEY_SWIPE_START_Y, DEFAULT_SWIPE_START_Y);
    }
    
    public void setSwipeStartY(int y) {
        preferences.edit().putInt(KEY_SWIPE_START_Y, y).apply();
    }
    
    public int getSwipeEndX() {
        return preferences.getInt(KEY_SWIPE_END_X, DEFAULT_SWIPE_END_X);
    }
    
    public void setSwipeEndX(int x) {
        preferences.edit().putInt(KEY_SWIPE_END_X, x).apply();
    }
    
    public int getSwipeEndY() {
        return preferences.getInt(KEY_SWIPE_END_Y, DEFAULT_SWIPE_END_Y);
    }
    
    public void setSwipeEndY(int y) {
        preferences.edit().putInt(KEY_SWIPE_END_Y, y).apply();
    }
    
    public int getSwipeDuration() {
        return preferences.getInt(KEY_SWIPE_DURATION, DEFAULT_SWIPE_DURATION);
    }
    
    public void setSwipeDuration(int duration) {
        preferences.edit().putInt(KEY_SWIPE_DURATION, duration).apply();
    }
    
    public int getClickLoopCount() {
        return preferences.getInt(KEY_CLICK_LOOP_COUNT, DEFAULT_CLICK_LOOP_COUNT);
    }
    
    public void setClickLoopCount(int count) {
        preferences.edit().putInt(KEY_CLICK_LOOP_COUNT, count).apply();
    }
    
    /**
     * 获取向左滑动启动等待时间（毫秒）
     */
    public int getSwipeWaitTime() {
        return preferences.getInt(KEY_SWIPE_WAIT_TIME, DEFAULT_SWIPE_WAIT_TIME);
    }
    
    /**
     * 设置向左滑动启动等待时间（毫秒）
     */
    public void setSwipeWaitTime(int milliseconds) {
        preferences.edit().putInt(KEY_SWIPE_WAIT_TIME, milliseconds).apply();
    }
    
    /**
     * 获取最多操作控件数量
     */
    public int getMaxControlCount() {
        return preferences.getInt(KEY_MAX_CONTROL_COUNT, DEFAULT_MAX_CONTROL_COUNT);
    }
    
    /**
     * 设置最多操作控件数量
     */
    public void setMaxControlCount(int count) {
        preferences.edit().putInt(KEY_MAX_CONTROL_COUNT, count).apply();
    }
    
    /**
     * 获取点击启动得物app按钮后的等待时间（毫秒）
     */
    public int getDewuAppWaitTime() {
        return preferences.getInt(KEY_DEWU_APP_WAIT_TIME, DEFAULT_DEWU_APP_WAIT_TIME);
    }
    
    /**
     * 设置点击启动得物app按钮后的等待时间（毫秒）
     */
    public void setDewuAppWaitTime(int milliseconds) {
        preferences.edit().putInt(KEY_DEWU_APP_WAIT_TIME, milliseconds).apply();
    }
    
    
    /**
     * 是否启用点击商品链接
     */
    public boolean isClickProductLinkEnabled() {
        return preferences.getBoolean(KEY_CLICK_PRODUCT_LINK, DEFAULT_CLICK_PRODUCT_LINK);
    }
    
    /**
     * 设置是否启用点击商品链接
     */
    public void setClickProductLinkEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_CLICK_PRODUCT_LINK, enabled).apply();
    }
    
    // ==================== 激活码相关设置 ====================
    
    /**
     * 获取激活码
     */
    public String getActivateCode() {
        return preferences.getString(KEY_ACTIVATE_CODE, DEFAULT_ACTIVATE_CODE);
    }
    
    /**
     * 设置激活码
     */
    public void setActivateCode(String activateCode) {
        preferences.edit().putString(KEY_ACTIVATE_CODE, activateCode).apply();
    }
    
    /**
     * 检查是否已激活（考虑3天周期）
     */
    public boolean isActivated() {
        boolean activated = preferences.getBoolean(KEY_IS_ACTIVATED, DEFAULT_IS_ACTIVATED);
        if (!activated) {
            return false;
        }
        
        // 检查是否超过有效期
        long activationTime = preferences.getLong(KEY_ACTIVATION_TIME, DEFAULT_ACTIVATION_TIME);
        long currentTime = System.currentTimeMillis();
        long validTimeInMillis = getValidTimeInMillis();
        
        if (currentTime - activationTime > validTimeInMillis) {
            // 超过有效期，需要重新激活
            return false;
        }
        
        return true;
    }
    
    /**
     * 设置激活状态
     */
    public void setActivated(boolean activated) {
        preferences.edit().putBoolean(KEY_IS_ACTIVATED, activated).apply();
        if (activated) {
            // 激活时记录当前时间
            setActivationTime(System.currentTimeMillis());
        }
    }
    
    /**
     * 设置激活状态和有效期
     */
    public void setActivated(boolean activated, String validTime) {
        preferences.edit()
                .putBoolean(KEY_IS_ACTIVATED, activated)
                .putString(KEY_VALID_TIME, validTime)
                .apply();
        if (activated) {
            // 激活时记录当前时间
            setActivationTime(System.currentTimeMillis());
        }
    }
    
    /**
     * 获取激活时间
     */
    public long getActivationTime() {
        return preferences.getLong(KEY_ACTIVATION_TIME, DEFAULT_ACTIVATION_TIME);
    }
    
    /**
     * 设置激活时间
     */
    public void setActivationTime(long activationTime) {
        preferences.edit().putLong(KEY_ACTIVATION_TIME, activationTime).apply();
    }
    
    /**
     * 获取有效期时间字符串
     */
    public String getValidTime() {
        return preferences.getString(KEY_VALID_TIME, DEFAULT_VALID_TIME);
    }
    
    /**
     * 设置有效期时间字符串
     */
    public void setValidTime(String validTime) {
        preferences.edit().putString(KEY_VALID_TIME, validTime).apply();
    }
    
    /**
     * 将有效期字符串转换为毫秒数
     */
    private long getValidTimeInMillis() {
        String validTime = getValidTime();
        try {
            if (validTime.endsWith("days")) {
                int days = Integer.parseInt(validTime.replace("days", ""));
                return days * 24 * 60 * 60 * 1000L;
            } else if (validTime.endsWith("hours")) {
                int hours = Integer.parseInt(validTime.replace("hours", ""));
                return hours * 60 * 60 * 1000L;
            } else if (validTime.endsWith("minutes")) {
                int minutes = Integer.parseInt(validTime.replace("minutes", ""));
                return minutes * 60 * 1000L;
            }
        } catch (NumberFormatException e) {
            // 解析失败，使用默认7天
        }
        return 7 * 24 * 60 * 60 * 1000L; // 默认7天
    }
    
    /**
     * 获取剩余激活时间（毫秒）
     */
    public long getRemainingActivationTime() {
        long activationTime = getActivationTime();
        long currentTime = System.currentTimeMillis();
        long validTimeInMillis = getValidTimeInMillis();
        
        long elapsed = currentTime - activationTime;
        return Math.max(0, validTimeInMillis - elapsed);
    }
    
    /**
     * 获取剩余激活天数
     */
    public int getRemainingActivationDays() {
        long remainingMillis = getRemainingActivationTime();
        return (int) (remainingMillis / (24 * 60 * 60 * 1000L));
    }
    
    /**
     * 获取剩余激活小时数
     */
    public int getRemainingActivationHours() {
        long remainingMillis = getRemainingActivationTime();
        return (int) ((remainingMillis % (24 * 60 * 60 * 1000L)) / (60 * 60 * 1000L));
    }
    
    /**
     * 清除激活信息
     */
    public void clearActivation() {
        preferences.edit()
                .remove(KEY_ACTIVATE_CODE)
                .putBoolean(KEY_IS_ACTIVATED, false)
                .putLong(KEY_ACTIVATION_TIME, DEFAULT_ACTIVATION_TIME)
                .putString(KEY_VALID_TIME, DEFAULT_VALID_TIME)
                .apply();
    }
}

