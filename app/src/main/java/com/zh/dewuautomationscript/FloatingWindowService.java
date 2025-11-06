package com.zh.dewuautomationscript;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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

import androidx.core.app.NotificationCompat;

/**
 * 悬浮窗服务
 * 用于在脚本执行期间显示悬浮窗，防止应用被后台杀死
 * 支持点击暂停/继续
 */
public class FloatingWindowService extends Service {
    
    private static final String TAG = "FloatingWindowService";
    private static FloatingWindowService instance;
    private static boolean isPaused = false;
    // 用于线程同步的对象锁
    private static final Object pauseLock = new Object();
    
    private WindowManager windowManager;
    private View floatingView;
    private TextView statusText;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        isPaused = false;
        
        // Android 8.0+ 前台服务支持
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                String channelId = "floating_window_channel";
                String channelName = "悬浮窗服务";
                
                NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_LOW
                );
                channel.setShowBadge(false);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setSound(null, null);
                
                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }
                
                Notification notification = new NotificationCompat.Builder(this, channelId)
                    .setContentTitle("脚本运行中")
                    .setContentText("正在执行自动化任务...")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();
                
                // Android 12+ 需要指定前台服务类型
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
                } else {
                    startForeground(1, notification);
                }
                Log.d(TAG, "前台服务已启动");
            } catch (Exception e) {
                Log.e(TAG, "启动前台服务失败", e);
            }
        }
        
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
        synchronized (pauseLock) {
            isPaused = !isPaused;
            Log.d(TAG, "脚本暂停状态切换为: " + isPaused);
            
            if (isPaused) {
                updateStatus("⏸ 已暂停（点击继续）");
            } else {
                updateStatus("▶ 继续执行中...");
                // 恢复时通知所有等待的线程
                pauseLock.notifyAll();
            }
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
        
        // Android 8.0+ 使用前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                context.startForegroundService(intent);
            } catch (IllegalStateException e) {
                Log.e(TAG, "启动前台服务失败: " + e.getMessage());
                // 降级到普通启动
                context.startService(intent);
            }
        } else {
            context.startService(intent);
        }
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
        synchronized (pauseLock) {
            isPaused = paused;
            if (instance != null) {
                if (isPaused) {
                    instance.updateStatus("⏸ 已暂停（点击继续）");
                } else {
                    instance.updateStatus("▶ 继续执行中...");
                    // 恢复时通知所有等待的线程
                    pauseLock.notifyAll();
                }
            }
        }
    }
    
    /**
     * 等待直到不暂停（使用真正的线程阻塞，而不是轮询）
     * 这个方法会阻塞当前线程，直到脚本恢复执行或脚本被停止
     */
    public static void waitIfPaused() {
        synchronized (pauseLock) {
            while (isPaused) {
                // 检查脚本是否被停止，如果停止则立即退出
                if (!AutomationScriptExecutor.isScriptRunningStatic()) {
                    Log.d(TAG, "脚本已停止，退出waitIfPaused");
                    break;
                }
                
                // 检查线程是否被中断
                if (Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "线程被中断，退出waitIfPaused");
                    break;
                }
                
                try {
                    // 使用 wait() 真正阻塞线程，而不是轮询
                    // 设置超时，以便定期检查脚本运行状态
                    pauseLock.wait(100); // 每100ms检查一次
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.d(TAG, "等待被中断");
                    break;
                }
            }
        }
    }
    
    /**
     * 检查是否暂停，不阻塞线程
     * @return true 如果暂停，false 如果未暂停
     */
    public static boolean checkIfPaused() {
        synchronized (pauseLock) {
            return isPaused;
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

