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
    private static final String KEY_ENTER_HOME_PAGE_WAIT_TIME = "enter_home_page_wait_time"; // 点击进入主页的按钮后需要等待多少秒
    private static final String KEY_SEARCH_PRODUCT_SWIPE_START_X = "search_product_swipe_start_x"; // 搜索商品时滑动起始X
    private static final String KEY_SEARCH_PRODUCT_SWIPE_START_Y = "search_product_swipe_start_y"; // 搜索商品时滑动起始Y
    private static final String KEY_SEARCH_PRODUCT_SWIPE_END_X = "search_product_swipe_end_x"; // 搜索商品时滑动结束X
    private static final String KEY_SEARCH_PRODUCT_SWIPE_END_Y_OFFSET = "search_product_swipe_end_y_offset"; // 搜索商品时滑动结束Y偏移量
    private static final String KEY_SEARCH_PRODUCT_SWIPE_DURATION = "search_product_swipe_duration"; // 搜索商品时滑动持续时间
    private static final String KEY_USER_HOME_PAGE_SWIPE_START_X = "user_home_page_swipe_start_x"; // 用户主页滑动起始X
    private static final String KEY_USER_HOME_PAGE_SWIPE_START_Y = "user_home_page_swipe_start_y"; // 用户主页滑动起始Y
    private static final String KEY_USER_HOME_PAGE_SWIPE_END_X = "user_home_page_swipe_end_x"; // 用户主页滑动结束X
    private static final String KEY_USER_HOME_PAGE_SWIPE_END_Y_OFFSET = "user_home_page_swipe_end_y_offset"; // 用户主页滑动结束Y偏移量（用于动态计算）
    private static final String KEY_USER_HOME_PAGE_SWIPE_DURATION = "user_home_page_swipe_duration"; // 用户主页滑动持续时间
    
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
    private static final int DEFAULT_ENTER_HOME_PAGE_WAIT_TIME = 2000; // 点击进入主页的按钮后等待2秒
    private static final int DEFAULT_SEARCH_PRODUCT_SWIPE_START_X = 0; // 0表示屏幕中心（screenWidth/2）
    private static final int DEFAULT_SEARCH_PRODUCT_SWIPE_START_Y = -400; // -400表示screenHeight-400（负数表示从底部偏移）
    private static final int DEFAULT_SEARCH_PRODUCT_SWIPE_END_X = 0; // 0表示屏幕中心（screenWidth/2）
    private static final int DEFAULT_SEARCH_PRODUCT_SWIPE_END_Y_OFFSET = -400; // -400表示screenHeight-scrollDistance-400（负数表示从底部偏移）
    private static final int DEFAULT_SEARCH_PRODUCT_SWIPE_DURATION = 2000; // 2000毫秒
    private static final int DEFAULT_USER_HOME_PAGE_SWIPE_START_X = 500; // 绝对坐标
    private static final int DEFAULT_USER_HOME_PAGE_SWIPE_START_Y = -400; // -400表示screenHeight-400（负数表示从底部偏移）
    private static final int DEFAULT_USER_HOME_PAGE_SWIPE_END_X = 500; // 绝对坐标
    private static final int DEFAULT_USER_HOME_PAGE_SWIPE_END_Y_OFFSET = -400; // -400表示screenHeight-scrollDistance-400（负数表示从底部偏移）
    private static final int DEFAULT_USER_HOME_PAGE_SWIPE_DURATION = 2000; // 2000毫秒
    
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
    
    /**
     * 获取点击进入主页的按钮后需要等待的时间（毫秒）
     */
    public int getEnterHomePageWaitTime() {
        return preferences.getInt(KEY_ENTER_HOME_PAGE_WAIT_TIME, DEFAULT_ENTER_HOME_PAGE_WAIT_TIME);
    }
    
    /**
     * 设置点击进入主页的按钮后需要等待的时间（毫秒）
     */
    public void setEnterHomePageWaitTime(int milliseconds) {
        preferences.edit().putInt(KEY_ENTER_HOME_PAGE_WAIT_TIME, milliseconds).apply();
    }
    
    // ==================== 搜索商品时滑动参数设置 ====================
    
    /**
     * 获取搜索商品时滑动起始X坐标（0表示屏幕中心，正数表示绝对坐标）
     */
    public int getSearchProductSwipeStartX() {
        return preferences.getInt(KEY_SEARCH_PRODUCT_SWIPE_START_X, DEFAULT_SEARCH_PRODUCT_SWIPE_START_X);
    }
    
    /**
     * 设置搜索商品时滑动起始X坐标（0表示屏幕中心，正数表示绝对坐标）
     */
    public void setSearchProductSwipeStartX(int x) {
        preferences.edit().putInt(KEY_SEARCH_PRODUCT_SWIPE_START_X, x).apply();
    }
    
    /**
     * 获取搜索商品时滑动起始Y坐标（负数表示从屏幕底部偏移，正数表示绝对坐标）
     */
    public int getSearchProductSwipeStartY() {
        return preferences.getInt(KEY_SEARCH_PRODUCT_SWIPE_START_Y, DEFAULT_SEARCH_PRODUCT_SWIPE_START_Y);
    }
    
    /**
     * 设置搜索商品时滑动起始Y坐标（负数表示从屏幕底部偏移，正数表示绝对坐标）
     */
    public void setSearchProductSwipeStartY(int y) {
        preferences.edit().putInt(KEY_SEARCH_PRODUCT_SWIPE_START_Y, y).apply();
    }
    
    /**
     * 获取搜索商品时滑动结束X坐标（0表示屏幕中心，正数表示绝对坐标）
     */
    public int getSearchProductSwipeEndX() {
        return preferences.getInt(KEY_SEARCH_PRODUCT_SWIPE_END_X, DEFAULT_SEARCH_PRODUCT_SWIPE_END_X);
    }
    
    /**
     * 设置搜索商品时滑动结束X坐标（0表示屏幕中心，正数表示绝对坐标）
     */
    public void setSearchProductSwipeEndX(int x) {
        preferences.edit().putInt(KEY_SEARCH_PRODUCT_SWIPE_END_X, x).apply();
    }
    
    /**
     * 获取搜索商品时滑动结束Y偏移量（负数表示从屏幕底部偏移，用于计算结束Y坐标）
     */
    public int getSearchProductSwipeEndYOffset() {
        return preferences.getInt(KEY_SEARCH_PRODUCT_SWIPE_END_Y_OFFSET, DEFAULT_SEARCH_PRODUCT_SWIPE_END_Y_OFFSET);
    }
    
    /**
     * 设置搜索商品时滑动结束Y偏移量（负数表示从屏幕底部偏移，用于计算结束Y坐标）
     */
    public void setSearchProductSwipeEndYOffset(int offset) {
        preferences.edit().putInt(KEY_SEARCH_PRODUCT_SWIPE_END_Y_OFFSET, offset).apply();
    }
    
    /**
     * 获取搜索商品时滑动持续时间（毫秒）
     */
    public int getSearchProductSwipeDuration() {
        return preferences.getInt(KEY_SEARCH_PRODUCT_SWIPE_DURATION, DEFAULT_SEARCH_PRODUCT_SWIPE_DURATION);
    }
    
    /**
     * 设置搜索商品时滑动持续时间（毫秒）
     */
    public void setSearchProductSwipeDuration(int duration) {
        preferences.edit().putInt(KEY_SEARCH_PRODUCT_SWIPE_DURATION, duration).apply();
    }
    
    // ==================== 用户主页滑动参数设置 ====================
    
    /**
     * 获取用户主页滑动起始X坐标（正数表示绝对坐标）
     */
    public int getUserHomePageSwipeStartX() {
        return preferences.getInt(KEY_USER_HOME_PAGE_SWIPE_START_X, DEFAULT_USER_HOME_PAGE_SWIPE_START_X);
    }
    
    /**
     * 设置用户主页滑动起始X坐标（正数表示绝对坐标）
     */
    public void setUserHomePageSwipeStartX(int x) {
        preferences.edit().putInt(KEY_USER_HOME_PAGE_SWIPE_START_X, x).apply();
    }
    
    /**
     * 获取用户主页滑动起始Y坐标（负数表示从屏幕底部偏移，正数表示绝对坐标）
     */
    public int getUserHomePageSwipeStartY() {
        return preferences.getInt(KEY_USER_HOME_PAGE_SWIPE_START_Y, DEFAULT_USER_HOME_PAGE_SWIPE_START_Y);
    }
    
    /**
     * 设置用户主页滑动起始Y坐标（负数表示从屏幕底部偏移，正数表示绝对坐标）
     */
    public void setUserHomePageSwipeStartY(int y) {
        preferences.edit().putInt(KEY_USER_HOME_PAGE_SWIPE_START_Y, y).apply();
    }
    
    /**
     * 获取用户主页滑动结束X坐标（正数表示绝对坐标）
     */
    public int getUserHomePageSwipeEndX() {
        return preferences.getInt(KEY_USER_HOME_PAGE_SWIPE_END_X, DEFAULT_USER_HOME_PAGE_SWIPE_END_X);
    }
    
    /**
     * 设置用户主页滑动结束X坐标（正数表示绝对坐标）
     */
    public void setUserHomePageSwipeEndX(int x) {
        preferences.edit().putInt(KEY_USER_HOME_PAGE_SWIPE_END_X, x).apply();
    }
    
    /**
     * 获取用户主页滑动结束Y偏移量（负数表示从屏幕底部偏移，用于动态计算结束Y坐标）
     */
    public int getUserHomePageSwipeEndYOffset() {
        return preferences.getInt(KEY_USER_HOME_PAGE_SWIPE_END_Y_OFFSET, DEFAULT_USER_HOME_PAGE_SWIPE_END_Y_OFFSET);
    }
    
    /**
     * 设置用户主页滑动结束Y偏移量（负数表示从屏幕底部偏移，用于动态计算结束Y坐标）
     */
    public void setUserHomePageSwipeEndYOffset(int offset) {
        preferences.edit().putInt(KEY_USER_HOME_PAGE_SWIPE_END_Y_OFFSET, offset).apply();
    }
    
    /**
     * 获取用户主页滑动持续时间（毫秒）
     */
    public int getUserHomePageSwipeDuration() {
        return preferences.getInt(KEY_USER_HOME_PAGE_SWIPE_DURATION, DEFAULT_USER_HOME_PAGE_SWIPE_DURATION);
    }
    
    /**
     * 设置用户主页滑动持续时间（毫秒）
     */
    public void setUserHomePageSwipeDuration(int duration) {
        preferences.edit().putInt(KEY_USER_HOME_PAGE_SWIPE_DURATION, duration).apply();
    }
}

