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
    
    private EditText etDewuAppWaitTime, etClickLoopCount, etMaxControlCount;
    private CheckBox cbEnableScroll, cbClickProductLink;
    private EditText etSwipeStartX, etSwipeStartY, etSwipeEndX, etSwipeEndY, etSwipeDuration, etSwipeWaitTime;
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
        btnSave = findViewById(R.id.btn_save_settings);
    }
    
    private void loadSettings() {
        // 加载当前设置
        int dewuAppWaitTime = settingsManager.getDewuAppWaitTime();
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
        
        etDewuAppWaitTime.setText(String.valueOf(dewuAppWaitTime / 1000)); // 转换为秒显示
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
            
            // 验证参数
            if (swipeDuration < 0 || swipeDuration > 5) {
                Toast.makeText(this, "滑动时长应在 0-5 秒之间", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (swipeWaitTime < 0 || swipeWaitTime > 5) {
                Toast.makeText(this, "滑动前等待时间应在 0-5 秒之间", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 保存设置
            settingsManager.setDewuAppWaitTime(dewuAppWaitTime * 1000); // 转换为毫秒
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

