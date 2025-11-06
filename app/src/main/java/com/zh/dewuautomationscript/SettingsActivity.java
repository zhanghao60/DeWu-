package com.zh.dewuautomationscript;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 设置界面
 */
public class SettingsActivity extends AppCompatActivity {
    
    private EditText etDewuAppWaitTime, etClickLoopCount, etMaxControlCount, etEnterHomePageWaitTime;
    private CheckBox cbEnableScroll, cbClickProductLink;
    private EditText etSwipeStartX, etSwipeStartY, etSwipeEndX, etSwipeEndY, etSwipeDuration, etSwipeWaitTime;
    private EditText etSearchProductSwipeStartX, etSearchProductSwipeStartY, etSearchProductSwipeEndX, etSearchProductSwipeEndYOffset, etSearchProductSwipeDuration;
    private EditText etUserHomePageSwipeStartX, etUserHomePageSwipeStartY, etUserHomePageSwipeEndX, etUserHomePageSwipeEndYOffset, etUserHomePageSwipeDuration;
    private Button btnSave;
    
    private SettingsManager settingsManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        settingsManager = new SettingsManager(this);
        
        initViews();
        loadSettings();
        setListeners();
    }
    
    private void initViews() {
        etDewuAppWaitTime = findViewById(R.id.et_dewu_app_wait_time);
        etEnterHomePageWaitTime = findViewById(R.id.et_enter_home_page_wait_time);
        etClickLoopCount = findViewById(R.id.et_click_loop_count);
        etMaxControlCount = findViewById(R.id.et_max_control_count);
        cbEnableScroll = findViewById(R.id.cb_enable_scroll);
        cbClickProductLink = findViewById(R.id.cb_click_product_link);
        etSwipeStartX = findViewById(R.id.et_swipe_start_x);
        etSwipeStartY = findViewById(R.id.et_swipe_start_y);
        etSwipeEndX = findViewById(R.id.et_swipe_end_x);
        etSwipeEndY = findViewById(R.id.et_swipe_end_y);
        etSwipeDuration = findViewById(R.id.et_swipe_duration);
        etSwipeWaitTime = findViewById(R.id.et_swipe_wait_time);
        etSearchProductSwipeStartX = findViewById(R.id.et_search_product_swipe_start_x);
        etSearchProductSwipeStartY = findViewById(R.id.et_search_product_swipe_start_y);
        etSearchProductSwipeEndX = findViewById(R.id.et_search_product_swipe_end_x);
        etSearchProductSwipeEndYOffset = findViewById(R.id.et_search_product_swipe_end_y_offset);
        etSearchProductSwipeDuration = findViewById(R.id.et_search_product_swipe_duration);
        etUserHomePageSwipeStartX = findViewById(R.id.et_user_home_page_swipe_start_x);
        etUserHomePageSwipeStartY = findViewById(R.id.et_user_home_page_swipe_start_y);
        etUserHomePageSwipeEndX = findViewById(R.id.et_user_home_page_swipe_end_x);
        etUserHomePageSwipeEndYOffset = findViewById(R.id.et_user_home_page_swipe_end_y_offset);
        etUserHomePageSwipeDuration = findViewById(R.id.et_user_home_page_swipe_duration);
        btnSave = findViewById(R.id.btn_save_settings);
    }
    
    private void loadSettings() {
        // 加载当前设置
        int dewuAppWaitTime = settingsManager.getDewuAppWaitTime();
        int enterHomePageWaitTime = settingsManager.getEnterHomePageWaitTime();
        int clickLoopCount = settingsManager.getClickLoopCount();
        int maxControlCount = settingsManager.getMaxControlCount();
        boolean swipeAfterClick = settingsManager.isSwipeAfterClickEnabled();
        boolean clickProductLink = settingsManager.isClickProductLinkEnabled();
        int swipeStartX = settingsManager.getSwipeStartX();
        int swipeStartY = settingsManager.getSwipeStartY();
        int swipeEndX = settingsManager.getSwipeEndX();
        int swipeEndY = settingsManager.getSwipeEndY();
        int swipeDuration = settingsManager.getSwipeDuration();
        int swipeWaitTime = settingsManager.getSwipeWaitTime();
        int searchProductSwipeStartX = settingsManager.getSearchProductSwipeStartX();
        int searchProductSwipeStartY = settingsManager.getSearchProductSwipeStartY();
        int searchProductSwipeEndX = settingsManager.getSearchProductSwipeEndX();
        int searchProductSwipeEndYOffset = settingsManager.getSearchProductSwipeEndYOffset();
        int searchProductSwipeDuration = settingsManager.getSearchProductSwipeDuration();
        int userHomePageSwipeStartX = settingsManager.getUserHomePageSwipeStartX();
        int userHomePageSwipeStartY = settingsManager.getUserHomePageSwipeStartY();
        int userHomePageSwipeEndX = settingsManager.getUserHomePageSwipeEndX();
        int userHomePageSwipeEndYOffset = settingsManager.getUserHomePageSwipeEndYOffset();
        int userHomePageSwipeDuration = settingsManager.getUserHomePageSwipeDuration();
        
        etDewuAppWaitTime.setText(String.valueOf(dewuAppWaitTime / 1000)); // 转换为秒显示
        etEnterHomePageWaitTime.setText(String.valueOf(enterHomePageWaitTime / 1000)); // 转换为秒显示
        etClickLoopCount.setText(String.valueOf(clickLoopCount));
        etMaxControlCount.setText(String.valueOf(maxControlCount));
        cbEnableScroll.setChecked(swipeAfterClick);
        cbClickProductLink.setChecked(clickProductLink);
        etSwipeStartX.setText(String.valueOf(swipeStartX));
        etSwipeStartY.setText(String.valueOf(swipeStartY));
        etSwipeEndX.setText(String.valueOf(swipeEndX));
        etSwipeEndY.setText(String.valueOf(swipeEndY));
        etSwipeDuration.setText(String.valueOf(swipeDuration / 1000.0)); // 转换为秒显示
        etSwipeWaitTime.setText(String.valueOf(swipeWaitTime / 1000.0)); // 转换为秒显示
        etSearchProductSwipeStartX.setText(String.valueOf(searchProductSwipeStartX));
        etSearchProductSwipeStartY.setText(String.valueOf(searchProductSwipeStartY));
        etSearchProductSwipeEndX.setText(String.valueOf(searchProductSwipeEndX));
        etSearchProductSwipeEndYOffset.setText(String.valueOf(searchProductSwipeEndYOffset));
        etSearchProductSwipeDuration.setText(String.valueOf(searchProductSwipeDuration));
        etUserHomePageSwipeStartX.setText(String.valueOf(userHomePageSwipeStartX));
        etUserHomePageSwipeStartY.setText(String.valueOf(userHomePageSwipeStartY));
        etUserHomePageSwipeEndX.setText(String.valueOf(userHomePageSwipeEndX));
        etUserHomePageSwipeEndYOffset.setText(String.valueOf(userHomePageSwipeEndYOffset));
        etUserHomePageSwipeDuration.setText(String.valueOf(userHomePageSwipeDuration));
        
    }
    
    
    private void setListeners() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }
    
    private void saveSettings() {
        try {
            // 获取点击启动得物app按钮之后的等待时间
            String dewuAppWaitTimeStr = etDewuAppWaitTime.getText().toString().trim();
            if (dewuAppWaitTimeStr.isEmpty()) {
                Toast.makeText(this, "请输入点击启动得物app按钮之后的等待时间", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int dewuAppWaitTime = Integer.parseInt(dewuAppWaitTimeStr);
            if (dewuAppWaitTime < 0 || dewuAppWaitTime > 60) {
                Toast.makeText(this, "点击启动得物app按钮之后的等待时间应在 0-60 秒之间", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 获取点击进入主页的按钮后需要等待的时间
            String enterHomePageWaitTimeStr = etEnterHomePageWaitTime.getText().toString().trim();
            if (enterHomePageWaitTimeStr.isEmpty()) {
                Toast.makeText(this, "请输入点击进入主页的按钮后需要等待的时间", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int enterHomePageWaitTime = Integer.parseInt(enterHomePageWaitTimeStr);
            if (enterHomePageWaitTime < 0 || enterHomePageWaitTime > 60) {
                Toast.makeText(this, "点击进入主页的按钮后需要等待的时间应在 0-60 秒之间", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 获取控件重复点击次数
            int clickLoopCount = parseIntOrDefault(etClickLoopCount.getText().toString(), 5);
            if (clickLoopCount < 1 || clickLoopCount > 20) {
                Toast.makeText(this, "控件重复点击次数应在 1-20 之间", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 获取最多操作控件数量
            int maxControlCount = parseIntOrDefault(etMaxControlCount.getText().toString(), 12);
            if (maxControlCount < 1 || maxControlCount > 50) {
                Toast.makeText(this, "最多操作控件数量应在 1-50 之间", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 获取点击后滑动设置
            boolean swipeAfterClick = cbEnableScroll.isChecked();
            boolean clickProductLink = cbClickProductLink.isChecked();
            
            
            // 获取滑动参数
            int swipeStartX = parseIntOrDefault(etSwipeStartX.getText().toString(), 800);
            int swipeStartY = parseIntOrDefault(etSwipeStartY.getText().toString(), 1200);
            int swipeEndX = parseIntOrDefault(etSwipeEndX.getText().toString(), 200);
            int swipeEndY = parseIntOrDefault(etSwipeEndY.getText().toString(), 1200);
            double swipeDuration = parseDoubleOrDefault(etSwipeDuration.getText().toString(), 0.3);
            double swipeWaitTime = parseDoubleOrDefault(etSwipeWaitTime.getText().toString(), 0.5);
            
            // 获取搜索商品时滑动参数
            int searchProductSwipeStartX = parseIntOrDefault(etSearchProductSwipeStartX.getText().toString(), 0);
            int searchProductSwipeStartY = parseIntOrDefault(etSearchProductSwipeStartY.getText().toString(), -400);
            int searchProductSwipeEndX = parseIntOrDefault(etSearchProductSwipeEndX.getText().toString(), 0);
            int searchProductSwipeEndYOffset = parseIntOrDefault(etSearchProductSwipeEndYOffset.getText().toString(), -400);
            int searchProductSwipeDuration = parseIntOrDefault(etSearchProductSwipeDuration.getText().toString(), 2000);
            
            // 获取用户主页滑动参数
            int userHomePageSwipeStartX = parseIntOrDefault(etUserHomePageSwipeStartX.getText().toString(), 500);
            int userHomePageSwipeStartY = parseIntOrDefault(etUserHomePageSwipeStartY.getText().toString(), -400);
            int userHomePageSwipeEndX = parseIntOrDefault(etUserHomePageSwipeEndX.getText().toString(), 500);
            int userHomePageSwipeEndYOffset = parseIntOrDefault(etUserHomePageSwipeEndYOffset.getText().toString(), -400);
            int userHomePageSwipeDuration = parseIntOrDefault(etUserHomePageSwipeDuration.getText().toString(), 2000);
            
            // 验证参数
            if (swipeDuration < 0 || swipeDuration > 5) {
                Toast.makeText(this, "滑动时长应在 0-5 秒之间", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (swipeWaitTime < 0 || swipeWaitTime > 5) {
                Toast.makeText(this, "滑动前等待时间应在 0-5 秒之间", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (searchProductSwipeDuration < 100 || searchProductSwipeDuration > 10000) {
                Toast.makeText(this, "搜索商品时滑动持续时间应在 100-10000 毫秒之间", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (userHomePageSwipeDuration < 100 || userHomePageSwipeDuration > 10000) {
                Toast.makeText(this, "用户主页滑动持续时间应在 100-10000 毫秒之间", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 保存设置
            settingsManager.setDewuAppWaitTime(dewuAppWaitTime * 1000); // 转换为毫秒
            settingsManager.setEnterHomePageWaitTime(enterHomePageWaitTime * 1000); // 转换为毫秒
            settingsManager.setClickLoopCount(clickLoopCount);
            settingsManager.setMaxControlCount(maxControlCount);
            settingsManager.setSwipeAfterClickEnabled(swipeAfterClick);
            settingsManager.setClickProductLinkEnabled(clickProductLink);
            settingsManager.setSwipeStartX(swipeStartX);
            settingsManager.setSwipeStartY(swipeStartY);
            settingsManager.setSwipeEndX(swipeEndX);
            settingsManager.setSwipeEndY(swipeEndY);
            settingsManager.setSwipeDuration((int)(swipeDuration * 1000)); // 转换为毫秒
            settingsManager.setSwipeWaitTime((int)(swipeWaitTime * 1000)); // 转换为毫秒
            settingsManager.setSearchProductSwipeStartX(searchProductSwipeStartX);
            settingsManager.setSearchProductSwipeStartY(searchProductSwipeStartY);
            settingsManager.setSearchProductSwipeEndX(searchProductSwipeEndX);
            settingsManager.setSearchProductSwipeEndYOffset(searchProductSwipeEndYOffset);
            settingsManager.setSearchProductSwipeDuration(searchProductSwipeDuration);
            settingsManager.setUserHomePageSwipeStartX(userHomePageSwipeStartX);
            settingsManager.setUserHomePageSwipeStartY(userHomePageSwipeStartY);
            settingsManager.setUserHomePageSwipeEndX(userHomePageSwipeEndX);
            settingsManager.setUserHomePageSwipeEndYOffset(userHomePageSwipeEndYOffset);
            settingsManager.setUserHomePageSwipeDuration(userHomePageSwipeDuration);
            
            Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
            finish();
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "数值格式错误", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存设置失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 解析整数，失败返回默认值
     */
    private int parseIntOrDefault(String str, int defaultValue) {
        try {
            if (str == null || str.trim().isEmpty()) {
                return defaultValue;
            }
            return Integer.parseInt(str.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * 解析小数，失败返回默认值
     */
    private double parseDoubleOrDefault(String str, double defaultValue) {
        try {
            if (str == null || str.trim().isEmpty()) {
                return defaultValue;
            }
            return Double.parseDouble(str.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

