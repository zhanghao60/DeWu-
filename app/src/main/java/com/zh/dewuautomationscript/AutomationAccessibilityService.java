package com.zh.dewuautomationscript;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.view.KeyEvent;
import android.graphics.Rect;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 自动化无障碍服务类
 * 提供点击、滑动等自动化功能
 * 继承安卓原生AccessibilityService类，实现自动化功能
 */
public class AutomationAccessibilityService extends AccessibilityService {
    private static AutomationAccessibilityService instance;
    private static final String TAG = "AutomationService";
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private WindowManager windowManager;
    private DisplayMetrics displayMetrics;
    private OkHttpClient httpClient;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        //其余初始化配置
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        httpClient = new OkHttpClient();
        Log.d(TAG, "无障碍服务已创建");
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 处理无障碍事件
    }
    @Override
    public void onInterrupt() {
        Log.d(TAG, "无障碍服务被中断");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "无障碍服务已销毁");
    }
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            info.notificationTimeout = 100;
        }
        setServiceInfo(info);
        Log.d(TAG, "无障碍服务已连接");
    }

    /**
     * 获取服务实例
     */
    public static AutomationAccessibilityService getInstance() {
        return instance;
    }
    
    /**
     * 检查服务是否正在运行
     */
    public static boolean isServiceRunning() {
        return instance != null;
    }
    
    
    // ==================== 功能实现 ====================
    
    /**
     * 点击操作 - 根据坐标点击
     * @param x X坐标
     * @param y Y坐标
     * @param duration 持续时间（毫秒）
     * @return 是否成功
     */
    public static boolean Click(int x, int y, int duration) {
        try {
            //检查无障碍服务
            if (instance == null) {
                Log.e(TAG, "无障碍服务未初始化");
                return false;
            }
            
            Log.d(TAG, "准备执行点击: (" + x + ", " + y + "), 持续时间: " + duration + "ms");
            
            //构建点击路径
            Path path = new Path();
            path.moveTo(x, y);
            
            //创建手势描述
            GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, duration);
            GestureDescription gesture = new GestureDescription.Builder()
                    .addStroke(stroke)
                    .build();
            
            //执行手势
            boolean success = instance.dispatchGesture(gesture, new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    Log.d(TAG, "✅ 点击操作完成: (" + x + ", " + y + ")");
                }
                
                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    Log.w(TAG, "❌ 点击操作被取消: (" + x + ", " + y + ")");
                    Log.w(TAG, "可能原因: 1.权限不足 2.坐标无效 3.系统限制 4.服务状态异常");
                }
            }, null);
            
            Log.d(TAG, "点击操作调度结果: " + success + " at (" + x + ", " + y + ")");
            return success;
        } catch (Exception e) {
            Log.e(TAG, "坐标(" + x + ", " + y + ")点击操作失败", e);
            return false;
        }
    }

        
    /**
     * 控件点击
     * @param element 目标控件
     * @return 是否成功
     */
    public static boolean ClickElement(AccessibilityNodeInfo element) {
        try {
            //检查无障碍服务
            if (instance == null) {
                Log.e(TAG, "无障碍服务未初始化");
                return false;
            }
            //检查控件是否为空
            if (element == null) {
                Log.e(TAG, "控件为空，无法点击");
                return false;
            }
            
            //检查控件是否可点击
            if (!element.isClickable()) {
                Log.w(TAG, "控件不可点击");
                return false;
            }
            
            //执行点击
            boolean success = element.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (success) {
                Log.d(TAG, "控件点击成功");
                return true;
            } else {
                Log.w(TAG, "控件点击失败");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "点击控件失败", e);
            return false;
        }
    }
    

    /**
     * 滑动操作
     * @param startX 起始X坐标
     * @param startY 起始Y坐标
     * @param endX 结束X坐标
     * @param endY 结束Y坐标
     * @param duration 持续时间（毫秒）
     * @return 是否成功
     */
    public static boolean Swipe(int startX, int startY, int endX, int endY, int duration) {
        try {
            //检查无障碍服务
            if (instance == null) {
                Log.e(TAG, "无障碍服务未初始化");
                return false;
            }
            
            //构建滑动路径
            Path path = new Path();
            path.moveTo(startX, startY);
            path.lineTo(endX, endY);
            
            //创建手势描述
            GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, duration);
            GestureDescription gesture = new GestureDescription.Builder()
                    .addStroke(stroke)
                    .build();
            
            //执行手势
            boolean success = instance.dispatchGesture(gesture, new AccessibilityService.GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    Log.d(TAG, "滑动操作完成: from (" + startX + ", " + startY + ") to (" + endX + ", " + endY + ")");
                }
                
                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    Log.w(TAG, "滑动操作被取消: from (" + startX + ", " + startY + ") to (" + endX + ", " + endY + ")");
                }
            }, null);
            
            Log.d(TAG, "滑动操作: " + success + " from (" + startX + ", " + startY + ") to (" + endX + ", " + endY + ")");
            return success;
        } catch (Exception e) {
            Log.e(TAG, "执行滑动操作失败", e);
            return false;
        }
    }
    

    /**
     * 返回操作
     * @return 是否成功
     */
    public static boolean GoBack() {
        try {
            //检查无障碍服务
            if (instance == null) {
                Log.e(TAG, "无障碍服务未初始化");
                return false;
            }
            
            //使用系统级全局操作执行返回
            boolean success = instance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
            
            if (success) {
                Log.d(TAG, "返回操作成功");
                return true;
            } else {
                Log.w(TAG, "返回操作失败，尝试使用按键事件");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "执行返回操作失败", e);
            return false;
        }
    }


    /**
     * 等待指定时间
     * @param milliseconds 毫秒数
     */
    public static void Sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Log.e(TAG, "Sleep被中断", e);
            Thread.currentThread().interrupt();
        }
    }
    

    /**
     * 获取当前页面所有控件
     * @return 控件列表（原生无障碍控件信息）
     */
    public static List<AccessibilityNodeInfo> GetAllElements() {
        List<AccessibilityNodeInfo> elements = new ArrayList<>();
        try {
            //检查无障碍服务
            if (instance == null) {
                Log.e(TAG, "无障碍服务未初始化");
                return elements;
            }
            
            AccessibilityNodeInfo root = instance.getRootInActiveWindow();
            if (root == null) {
                Log.e(TAG, "无法获取根节点");
                return elements;
            }
            
            // 使用栈进行非递归遍历
            java.util.Stack<AccessibilityNodeInfo> stack = new java.util.Stack<>();
            stack.push(root);
            
            while (!stack.isEmpty()) {
                AccessibilityNodeInfo node = stack.pop();
                if (node != null) {
                    // 添加当前节点
                    elements.add(AccessibilityNodeInfo.obtain(node));
                    
                    // 将子节点压入栈
                    int childCount = node.getChildCount();
                    for (int i = childCount - 1; i >= 0; i--) {
                        AccessibilityNodeInfo child = node.getChild(i);
                        if (child != null) {
                            stack.push(child);
                        }
                    }
                }
            }
            root.recycle();
        } catch (Exception e) {
            Log.e(TAG, "获取控件列表失败", e);
        }
        return elements;
    }
  

    /**
     * 根据viewId查找节点列表
     * @param elements 节点列表
     * @param viewId 目标viewId
     * @return 找到的节点列表，如果没有找到返回空列表
     */
    public static List<AccessibilityNodeInfo> findElementListById(List<AccessibilityNodeInfo> elements, String viewId) {
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        if (elements == null || viewId == null) return result;
        
        for (AccessibilityNodeInfo element : elements) {
            if (element == null) continue;
            
            String elementViewId = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                try {
                    elementViewId = element.getViewIdResourceName();
                } catch (Exception ignore) {
                }
            }   
            
            if (viewId.equals(elementViewId)) {
                result.add(element);
            }
        }
        
        return result;
    }
    

    /**
     * 根据文本内容查找节点列表
     * @param elements 节点列表
     * @param text 目标文本
     * @return 找到的节点列表，如果没有找到返回空列表
     */
    public static List<AccessibilityNodeInfo> findElementListByText(List<AccessibilityNodeInfo> elements, String text) {
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        if (elements == null || text == null) return result;
        
        for (AccessibilityNodeInfo element : elements) {
            if (element == null) continue;
            
            CharSequence nodeText = element.getText();
            if (nodeText != null && text.equals(nodeText.toString())) {
                result.add(element);
            }
            // 也检查contentDescription
            CharSequence desc = element.getContentDescription();
            if (desc != null && text.equals(desc.toString())) {
                result.add(element);
            }
        }
        return result;
    }
    

    /**
     * 根据类名查找节点列表
     * @param elements 节点列表
     * @param className 目标类名（支持部分匹配）
     * @return 找到的节点列表
     */
    public static List<AccessibilityNodeInfo> findElementListByClassName(List<AccessibilityNodeInfo> elements, String className) {
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        if (elements == null || className == null) return result;
        
        for (AccessibilityNodeInfo element : elements) {
            if (element == null) continue;
            
            CharSequence elementClassName = element.getClassName();
            if (elementClassName != null && elementClassName.toString().contains(className)) {
                result.add(element);
            }
        }
        
        return result;
    }


    /**
     * 批量回收AccessibilityNodeInfo列表中的所有节点
     * @param elements 要回收的节点列表
     */
    public static void recycleElementList(List<AccessibilityNodeInfo> elements) {
        if (elements == null) return;
        
        for (AccessibilityNodeInfo element : elements) {
            try {
                if (element != null) {
                    element.recycle();
                }
            } catch (Exception e) {
                Log.w(TAG, "回收节点时发生异常", e);
            }
        }
        elements.clear();
    }
}
