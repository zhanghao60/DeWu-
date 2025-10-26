package com.zh.dewuautomationscript;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * 悬浮窗服务
 * 用于在脚本执行期间显示悬浮窗，防止应用被后台杀死
 * 支持点击暂停/继续
 */
public class FloatingWindowService extends Service {
    
    private static final String TAG = "FloatingWindowService";
    private static FloatingWindowService instance;
    private static boolean isPaused = false;
    
    private WindowManager windowManager;
    private View floatingView;
    private TextView statusText;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        isPaused = false;
        Log.d(TAG, "FloatingWindowService created");
        createFloatingWindow();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String status = intent != null ? intent.getStringExtra("status") : "执行中...";
        Log.d(TAG, "onStartCommand called with status: " + status);
        updateStatus(status);
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        isPaused = false;
        if (floatingView != null && windowManager != null) {
            windowManager.removeView(floatingView);
            floatingView = null;
        }
        Log.d(TAG, "FloatingWindowService destroyed");
    }
    
    /**
     * 创建悬浮窗
     */
    private void createFloatingWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // 创建悬浮窗布局
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null);
        statusText = floatingView.findViewById(R.id.status_text);
        
        // 设置悬浮窗参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = 20;
        params.y = 100;
        
        // 设置点击事件
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private float initialX, initialY;
            private float initialTouchX, initialTouchY;
            private long touchStartTime;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        touchStartTime = System.currentTimeMillis();
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                        // 判断是点击还是拖动
                        long touchDuration = System.currentTimeMillis() - touchStartTime;
                        float deltaX = Math.abs(event.getRawX() - initialTouchX);
                        float deltaY = Math.abs(event.getRawY() - initialTouchY);
                        
                        // 如果移动距离小且时间短，判断为点击
                        if (deltaX < 10 && deltaY < 10 && touchDuration < 200) {
                            togglePause();
                        }
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        // 拖动悬浮窗
                        params.x = (int) (initialX + (initialTouchX - event.getRawX()));
                        params.y = (int) (initialY + (event.getRawY() - initialTouchY));
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
        
        // 添加悬浮窗
        try {
            windowManager.addView(floatingView, params);
            Log.d(TAG, "Floating window created successfully");
            if (statusText != null) {
                Log.d(TAG, "statusText initialized: " + (statusText != null));
            } else {
                Log.e(TAG, "statusText is null after inflating layout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create floating window", e);
        }
    }
    
    /**
     * 切换暂停状态
     */
    private void togglePause() {
        isPaused = !isPaused;
        Log.d(TAG, "脚本暂停状态切换为: " + isPaused);
        
        if (isPaused) {
            updateStatus("⏸ 已暂停（点击继续）");
        } else {
            updateStatus("▶ 继续执行中...");
        }
    }
    
    /**
     * 更新状态文本
     */
    public void updateStatus(String status) {
        if (statusText != null) {
            statusText.post(() -> {
                statusText.setText(status);
                Log.d(TAG, "悬浮窗状态更新: " + status);
            });
        } else {
            Log.w(TAG, "statusText为null，无法更新状态: " + status);
        }
    }
    
    /**
     * 启动悬浮窗服务
     */
    public static void startService(Context context, String status) {
        Intent intent = new Intent(context, FloatingWindowService.class);
        intent.putExtra("status", status);
        context.startService(intent);
    }
    
    /**
     * 更新悬浮窗状态
     */
    public static void updateService(Context context, String status) {
        if (instance != null && instance.statusText != null) {
            // 如果服务实例存在，直接更新状态
            instance.updateStatus(status);
        } else {
            // 如果服务实例不存在，启动服务
            Intent intent = new Intent(context, FloatingWindowService.class);
            intent.putExtra("status", status);
            context.startService(intent);
        }
    }
    
    /**
     * 停止悬浮窗服务
     */
    public static void stopService(Context context) {
        Intent intent = new Intent(context, FloatingWindowService.class);
        context.stopService(intent);
    }
    
    /**
     * 检查是否暂停
     */
    public static boolean isPaused() {
        return isPaused;
    }
    
    /**
     * 设置暂停状态
     */
    public static void setPaused(boolean paused) {
        isPaused = paused;
        if (instance != null) {
            if (isPaused) {
                instance.updateStatus("⏸ 已暂停（点击继续）");
            } else {
                instance.updateStatus("▶ 继续执行中...");
            }
        }
    }
    
    /**
     * 等待直到不暂停
     */
    public static void waitIfPaused() {
        while (isPaused) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * 测试悬浮窗是否正常工作
     */
    public static void testFloatingWindow(Context context) {
        Log.d(TAG, "Testing floating window...");
        updateService(context, "测试悬浮窗显示");
        
        // 延迟3秒后更新状态
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            updateService(context, "悬浮窗测试成功！");
        }, 3000);
    }
}

