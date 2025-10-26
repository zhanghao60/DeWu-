package com.zh.dewuautomationscript;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private Button btnStartScript, btnStopScript, btnManageLinks, btnHelpDocument, btnSettings, btnAccessibilityService;
    private AutomationScriptExecutor scriptExecutor;
    private SettingsManager settingsManager;
    private ActivationApiService activationApiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        try {
            initViews();
            initData();
            setClickListeners();
        } catch (Exception e) {
            Toast.makeText(this, "应用初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initViews() {
        btnStartScript = findViewById(R.id.btnStartScript);
        btnStopScript = findViewById(R.id.btnStopScript);
        btnManageLinks = findViewById(R.id.btnManageLinks);
        btnHelpDocument = findViewById(R.id.btnHelpDocument);
        btnSettings = findViewById(R.id.btnSettings);
        btnAccessibilityService = findViewById(R.id.btnAccessibilityService);
    }
    
    private void initData() {
        try {
            scriptExecutor = new AutomationScriptExecutor(this);
            settingsManager = new SettingsManager(this);
            activationApiService = new ActivationApiService();
            
            // 检查激活状态
            checkActivationStatus();
        } catch (Exception e) {
            Log.e("MainActivity", "初始化数据失败", e);
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    
    private void setClickListeners() {
        btnAccessibilityService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 使用高级方法直接跳转到应用的无障碍服务设置页面
                    AccessibilityCheck.openAppAccessibilitySettingsAdvanced(MainActivity.this);
                    Toast.makeText(MainActivity.this, "正在跳转到应用无障碍服务设置...", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("MainActivity", "打开无障碍设置失败", e);
                    Toast.makeText(MainActivity.this, "打开无障碍设置失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        btnStartScript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 检查MIUI兼容性
                    if (!MiuiCompatibilityHelper.checkAccessibilityCompatibility(MainActivity.this)) {
                        Toast.makeText(MainActivity.this, "系统兼容性检查失败", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    // 检查MIUI特殊权限
                    if (MiuiCompatibilityHelper.isMiui() && !MiuiCompatibilityHelper.checkMiuiPermissions(MainActivity.this)) {
                        String message = "MIUI系统权限检查失败\n\n" + MiuiCompatibilityHelper.getMiuiSuggestions();
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                        AccessibilityCheck.openAccessibilitySettings(MainActivity.this);
                        return;
                    }
                    
                    // 检查悬浮窗权限
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
                        Toast.makeText(MainActivity.this, "请授予悬浮窗权限以保持脚本在后台执行", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                        return;
                    }
                    
                    // 检查无障碍服务是否启用
                    if (AccessibilityCheck.isAccessibilityServiceEnabled(MainActivity.this)) {
                        // 直接启动脚本，使用无障碍服务截屏
                        executeAutomationScript();
                    } else {
                        String message = "请先启用无障碍服务";
                        if (MiuiCompatibilityHelper.isMiui()) {
                            message += "\n\n" + MiuiCompatibilityHelper.getMiuiSuggestions();
                        }
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                        AccessibilityCheck.openAccessibilitySettings(MainActivity.this);
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "启动脚本时发生错误", e);
                    Toast.makeText(MainActivity.this, "启动脚本失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        
        btnStopScript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d("MainActivity", "停止脚本按钮被点击");
                    if (scriptExecutor != null) {
                        scriptExecutor.testStopFunction();
                    }
                    stopAutomationScript();
                } catch (Exception e) {
                    Log.e("MainActivity", "停止脚本时发生错误", e);
                    Toast.makeText(MainActivity.this, "停止脚本失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        
        btnManageLinks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 跳转到URL管理界面
                    Intent intent = new Intent(MainActivity.this, UrlManagerActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("MainActivity", "打开URL管理界面失败", e);
                    Toast.makeText(MainActivity.this, "打开URL管理界面失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        btnHelpDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 跳转到说明文档界面
                    Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("MainActivity", "打开帮助文档失败", e);
                    Toast.makeText(MainActivity.this, "打开帮助文档失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 跳转到设置界面
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("MainActivity", "打开设置界面失败", e);
                    Toast.makeText(MainActivity.this, "打开设置界面失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    /**
     * 执行自动化脚本
     */
    private void executeAutomationScript() {
        Toast.makeText(this, "开始执行自动化脚本...", Toast.LENGTH_SHORT).show();
        
        scriptExecutor.executeScript(new AutomationScriptExecutor.ScriptCallback() {
            @Override
            public void onScriptStarted() {
                runOnUiThread(() -> {
                    btnStartScript.setEnabled(false);
                    btnStartScript.setText("脚本执行中...");
                    btnStopScript.setVisibility(View.VISIBLE);
                });
            }
            
            @Override
            public void onUrlProcessed(int current, int total, String url) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, 
                        String.format("处理第 %d/%d 个URL: %s", current, total, url), 
                        Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onRequestSent(String url, boolean success, String response) {
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(MainActivity.this, "请求发送成功: " + url, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "请求发送失败: " + url, Toast.LENGTH_LONG).show();
                    }
                });
            }
            
            @Override
            public void onClickPerformed(String url, int x, int y, boolean success) {
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(MainActivity.this, 
                            String.format("点击成功: (%d, %d) - %s", x, y, url), 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, 
                            String.format("点击失败: (%d, %d) - %s", x, y, url), 
                            Toast.LENGTH_LONG).show();
                    }
                });
            }
            
            @Override
            public void onScriptCompleted(boolean success, String message) {
                runOnUiThread(() -> {
                    btnStartScript.setEnabled(true);
                    btnStartScript.setText("启动脚本");
                    btnStopScript.setVisibility(View.GONE);
                    
                    // 根据消息内容显示不同的提示
                    if (message.contains("已停止")) {
                        Toast.makeText(MainActivity.this, "脚本已停止", Toast.LENGTH_SHORT).show();
                    } else if (success) {
                        Toast.makeText(MainActivity.this, "脚本执行完成: " + message, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "脚本执行失败: " + message, Toast.LENGTH_LONG).show();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnStartScript.setEnabled(true);
                    btnStartScript.setText("启动脚本");
                    btnStopScript.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "错误: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * 停止自动化脚本
     */
    private void stopAutomationScript() {
        try {
            Log.d("MainActivity", "用户点击停止脚本");
            
            if (scriptExecutor != null) {
                scriptExecutor.stopScript();
            }
            
            // UI状态更新会通过回调自动处理
            
        } catch (Exception e) {
            Log.e("MainActivity", "停止脚本失败", e);
            Toast.makeText(this, "停止脚本失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // 暂停时停止脚本执行
        if (scriptExecutor != null) {
            // 可以添加暂停脚本的逻辑
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 恢复时检查无障碍服务状态
        if (btnStartScript != null) {
            boolean isServiceEnabled = AccessibilityCheck.isAccessibilityServiceEnabled(this);
            btnStartScript.setEnabled(isServiceEnabled);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (scriptExecutor != null) {
                scriptExecutor.release();
                scriptExecutor = null;
            }
            if (activationApiService != null) {
                activationApiService.release();
                activationApiService = null;
            }
        } catch (Exception e) {
            Log.e("MainActivity", "清理资源时发生错误", e);
        }
    }
    
    /**
     * 检查激活状态
     */
    private void checkActivationStatus() {
        if (!settingsManager.isActivated()) {
            showActivationDialog();
        }
    }
    
    /**
     * 显示激活码输入对话框
     */
    private void showActivationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("软件激活");
        
        // 简化激活对话框消息
        String message = "请输入激活码以使用本软件";
        
        builder.setMessage(message);
        
        // 创建输入框
        final EditText input = new EditText(this);
        input.setHint("请输入激活码");
        builder.setView(input);
        
        builder.setPositiveButton("激活", (dialog, which) -> {
            String activateCode = input.getText().toString().trim();
            if (activateCode.isEmpty()) {
                Toast.makeText(this, "激活码不能为空", Toast.LENGTH_SHORT).show();
                showActivationDialog(); // 重新显示对话框
            } else {
                verifyActivateCode(activateCode);
            }
        });
        
        builder.setNegativeButton("退出", (dialog, which) -> {
            finish(); // 退出应用
        });
        
        builder.setCancelable(false); // 不允许取消对话框
        builder.show();
    }
    
    /**
     * 格式化时间显示
     */
    private String formatTime(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
    
    /**
     * 验证激活码
     */
    private void verifyActivateCode(String activateCode) {
        Toast.makeText(this, "正在验证激活码...", Toast.LENGTH_SHORT).show();
        
        activationApiService.verifyActivateCode(activateCode, new ActivationApiService.ActivationCallback() {
            @Override
            public void onSuccess(String message, String validTime) {
                runOnUiThread(() -> {
                    // 激活成功，保存激活信息和有效期
                    settingsManager.setActivateCode(activateCode);
                    settingsManager.setActivated(true, validTime);
                    
                    Toast.makeText(MainActivity.this, "激活成功！有效期" + validTime, Toast.LENGTH_SHORT).show();
                    Log.d("MainActivity", "激活码验证成功: " + activateCode + ", 有效期: " + validTime);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "激活失败: " + error, Toast.LENGTH_LONG).show();
                    Log.e("MainActivity", "激活码验证失败: " + error);
                    
                    // 激活失败，重新显示激活对话框
                    showActivationDialog();
                });
            }
        });
    }
}
