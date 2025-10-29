package com.zh.dewuautomationscript;

import android.view.accessibility.AccessibilityNodeInfo;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;


/**
 * 自动化脚本执行器
 */
public class AutomationScriptExecutor {
    
    // 初始化变量
    private static final String TAG = "AutomationScriptExecutor";
    private Context context;
    private JsonFileManager jsonFileManager;
    private OkHttpClient httpClient;
    private ExecutorService executorService;
    private Handler mainHandler;
    private AutomationAccessibilityService automationService;
    private SettingsManager settingsManager;
    private volatile boolean isScriptRunning = false;
    private ScriptCallback currentCallback;
    private volatile Thread scriptThread;
    
    // 回调接口
    public interface ScriptCallback {
        void onScriptStarted();
        void onUrlProcessed(int current, int total, String url);
        void onRequestSent(String url, boolean success, String response);
        void onClickPerformed(String url, int x, int y, boolean success);
        void onScriptCompleted(boolean success, String message);
        void onError(String error);
    }
    

    /**
     * 构造函数，初始化自动化脚本执行器
     * @param context Android上下文对象
     * @throws RuntimeException 如果初始化失败
     */
    public AutomationScriptExecutor(Context context) {
        try {
            this.context = context;
            this.jsonFileManager = new JsonFileManager(context);
            this.httpClient = new OkHttpClient();
            this.executorService = Executors.newSingleThreadExecutor();
            this.mainHandler = new Handler(Looper.getMainLooper());
            this.automationService = AutomationAccessibilityService.getInstance();
            this.settingsManager = new SettingsManager(context);
        } catch (Exception e) {
            Log.e(TAG, "AutomationScriptExecutor初始化失败", e);
            throw new RuntimeException("初始化失败: " + e.getMessage(), e);
        }
    }
    

    /**
     * 检查是否为得物深度链接
     * 判断URL是否以dewulink://开头，表示这是得物的深度链接
     * @param url 要检查的URL
     * @return true=是得物深度链接，false=不是
     */
    private boolean isDewuDeepLink(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return url.toLowerCase().startsWith("dewulink://");
    }
    

    /**
     * 处理得物深度链接
     * 直接打开得物app并执行自动化操作，跳过浏览器中转步骤
     * @param url 得物深度链接
     * @param callback 脚本回调接口
     * @param isLastUrl 是否为最后一个URL
     */
    private void processDewuDeepLink(String url, ScriptCallback callback, boolean isLastUrl) {
        try {
            // 检查脚本是否被停止
            if (!isScriptRunning) {
                Log.d(TAG, "脚本被停止，退出深度链接处理");
                return;
            }
            
            Log.d(TAG, "开始处理得物深度链接: " + url);
            
            // 更新悬浮窗状态
            FloatingWindowService.updateService(context, "直接打开得物app...");
            
            // 1. 直接打开得物app
            boolean openSuccess = openDewuApp(url);
            if (!openSuccess) {
                Log.e(TAG, "打开得物app失败");
                mainHandler.post(() -> callback.onError("无法打开得物app，请确保已安装得物应用"));
                return;
            }
            
            Log.d(TAG, "得物app打开成功");
            
            // 2. 等待得物app加载完成
            int dewuAppWaitTime = settingsManager.getDewuAppWaitTime();
            Log.d(TAG, "等待得物app加载" + (dewuAppWaitTime / 1000) + "秒...");
            
            // 更新悬浮窗状态
            FloatingWindowService.updateService(context, "等待得物app加载...");
            
            // 检查暂停状态
            FloatingWindowService.waitIfPaused();
            
            automationService.Sleep(dewuAppWaitTime);
            
            Log.d(TAG, "得物app加载完成，开始查找控件");
            
            // 检查暂停状态
            FloatingWindowService.waitIfPaused();
            
            // 3. 直接开始查找和操作控件
            String targetFullId = "com.shizhuang.duapp:id/imgPhoto";
            Log.d(TAG, "开始查找并操作目标控件: " + targetFullId);
            
            // 更新悬浮窗状态
            FloatingWindowService.updateService(context, "查找控件中...");
            
            // 开始操作
            findAndOperateControls(targetFullId);
            
            // 4. 调用点击完成回调
            mainHandler.post(() -> callback.onClickPerformed(url, -1, -1, true));
            
            Log.d(TAG, "得物深度链接处理完成");
            
        } catch (Exception e) {
            Log.e(TAG, "处理得物深度链接失败", e);
            mainHandler.post(() -> callback.onError("处理得物深度链接失败: " + e.getMessage()));
        }
    }
    
    /**
     * 打开得物app
     * 使用Intent直接启动得物应用并打开指定页面
     * @param url 得物深度链接
     * @return true=成功打开，false=打开失败
     */
    private boolean openDewuApp(String url) {
        try {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url));
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // 设置得物app的包名，确保直接打开得物app
            intent.setPackage("com.shizhuang.duapp");
            
            // 检查是否有应用可以处理这个Intent
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                Log.d(TAG, "成功打开得物app: " + url);
                return true;
            } else {
                Log.e(TAG, "得物app未安装或无法处理该深度链接");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "打开得物app失败", e);
            return false;
        }
    }

    /**
     * 停止脚本执行
     */
    public void stopScript() {
        Log.d(TAG, "停止脚本执行");
        isScriptRunning = false;
        
        // 中断脚本执行线程
        if (scriptThread != null && scriptThread.isAlive()) {
            Log.d(TAG, "中断脚本执行线程");
            scriptThread.interrupt();
        }
        
        // 通知回调脚本被停止
        if (currentCallback != null) {
            try {
                mainHandler.post(() -> {
                    currentCallback.onScriptCompleted(true, "脚本已停止");
                });
            } catch (Exception e) {
                Log.e(TAG, "通知回调失败", e);
            }
        }
        
        // 停止悬浮窗服务
        try {
            FloatingWindowService.stopService(context);
        } catch (Exception e) {
            Log.e(TAG, "停止悬浮窗服务失败", e);
        }
        
        Log.d(TAG, "脚本已停止");
    }
    
    /**
     * 检查脚本是否正在运行
     */
    public boolean isScriptRunning() {
        return isScriptRunning;
    }

    /**
     * 执行自动化脚本
     * 主要流程：读取URL列表 ，打开得物app，执行点击操作，滑动，返回
     * @param callback 脚本执行回调接口，用于通知执行状态和结果
     */
    public void executeScript(ScriptCallback callback) {
        // 设置脚本运行状态
        isScriptRunning = true;
        currentCallback = callback;
        
        // 启动悬浮窗，让系统认为应用正在使用
        FloatingWindowService.startService(context, "脚本初始化中...");
        Log.d(TAG, "已启动悬浮窗");
        
        executorService.execute(() -> {
            // 保存当前线程引用
            scriptThread = Thread.currentThread();
            
            try {
                mainHandler.post(() -> callback.onScriptStarted());
                
                // 1. 从JSON文件读取URL列表
                List<UrlItem> urlList = jsonFileManager.loadUrlsFromFile();
                if (urlList.isEmpty()) {
                    mainHandler.post(() -> callback.onError("没有找到保存的URL，请先添加URL"));
                    return;
                }
                
                Log.d(TAG, "开始执行脚本，共 " + urlList.size() + " 个URL");
                
                // 2. 逐个处理URL
                for (int i = 0; i < urlList.size(); i++) {
                    // 检查线程是否被中断
                    if (Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "脚本线程被中断，退出执行");
                        mainHandler.post(() -> callback.onScriptCompleted(true, "脚本已停止"));
                        return;
                    }
                    
                    // 检查脚本是否被停止
                    if (!isScriptRunning) {
                        Log.d(TAG, "脚本被停止，退出执行");
                        mainHandler.post(() -> callback.onScriptCompleted(true, "脚本已停止"));
                        return;
                    }
                    
                    // 检查暂停状态
                    FloatingWindowService.waitIfPaused();
                    
                    final int currentIndex = i;
                    final UrlItem urlItem = urlList.get(i);
                    final String url = urlItem.getUrl();
                    
                    // 更新悬浮窗状态
                    FloatingWindowService.updateService(context, "处理 " + (currentIndex + 1) + "/" + urlList.size());
                    
                    mainHandler.post(() -> callback.onUrlProcessed(currentIndex + 1, urlList.size(), url));
                    
                    // 3. 检查是否为得物深度链接
                    if (isDewuDeepLink(url)) {
                        Log.d(TAG, "检测到得物深度链接，直接打开得物app: " + url);
                        processDewuDeepLink(url, callback, i == urlList.size() - 1);
                    } else {
                       Log.d(TAG, "检测到普通链接,不执行自动化脚本");
                    }
                }
                
                mainHandler.post(() -> {
                    isScriptRunning = false;
                    scriptThread = null;
                    currentCallback = null;
                    callback.onScriptCompleted(true, "脚本执行完成");
                    // 所有任务完成后停止悬浮窗
                    FloatingWindowService.stopService(context);
                    Log.d(TAG, "已停止悬浮窗");
                });
                
            } catch (Exception e) {
                Log.e(TAG, "脚本执行失败", e);
                isScriptRunning = false;
                scriptThread = null;
                currentCallback = null;
                mainHandler.post(() -> callback.onError("脚本执行失败: " + e.getMessage()));
                // 出错也要停止悬浮窗
                FloatingWindowService.stopService(context);
            }
        });
    }
    
    /**
     * 查找并操作控件
     * 主要流程：查找目标控件，点击，滑动，返回
     * @param targetFullId 目标控件的完整ID
     */
    private void findAndOperateControls(String targetFullId) {
        try {
            // 检查暂停状态
            FloatingWindowService.waitIfPaused();
            //初始化文本列表，用于去重
            List<String> textList = new ArrayList<>();
            //初始化textViewNodes列表
            List<AccessibilityNodeInfo> textViewNodes = new ArrayList<>();
            CircularSearchElements(textViewNodes,textList);
            } catch (Exception e) {
                Log.e(TAG, "查找和操作控件时出错", e);
            }
        }
    
    
    /**
     * 
     * @param textViewNodes 文本节点列表
     * @param textList 文本列表
     * @return 是否找到控件
     */
    private void CircularSearchElements(List<AccessibilityNodeInfo> textViewNodes,List<String> textList) {
            // 检查线程是否被中断
            if (Thread.currentThread().isInterrupted()) {
                Log.d(TAG, "脚本线程被中断，退出控件查找");
                return;
            }
            
            // 检查脚本是否被停止
            if (!isScriptRunning) {
                Log.d(TAG, "脚本被停止，退出控件查找");
                return;
            }
            
            // 检查暂停状态
            FloatingWindowService.waitIfPaused();
            
            //获取所有id为com.shizhuang.duapp:id/tvTitle的TextView节点
            Log.d(TAG, "=== 开始查找控件 ===");
            FloatingWindowService.updateService(context, "正在查找控件...");
            
            // 获取所有元素
            List<AccessibilityNodeInfo> allElements = AutomationAccessibilityService.GetAllElements();
            if (allElements == null || allElements.isEmpty()) {
                Log.w(TAG, "无法获取页面元素，可能页面未加载完成");
                FloatingWindowService.updateService(context, "页面未加载完成，请稍后重试");
                return;
            }
            
            //获取所有id为com.shizhuang.duapp:id/tvTitle的TextView节点
            textViewNodes = AutomationAccessibilityService.findElementListById(allElements, "com.shizhuang.duapp:id/tvTitle");
            if (textViewNodes == null) {
                textViewNodes = new ArrayList<>();
            }
            
            Log.d(TAG, "找到 " + textViewNodes.size() + " 个tvTitle TextView节点");
            FloatingWindowService.updateService(context, "找到 " + textViewNodes.size() + " 个控件");

            // 打印所有找到的文本
            for (AccessibilityNodeInfo node : textViewNodes) {
                String nodeText = node.getText() != null ? node.getText().toString() : "无文本";
                Log.d(TAG, "找到的文本: " + nodeText);
            }
            
            //遍历textViewNodes，找到对应的Target节点
            int processedCount = 0; // 已处理的控件计数
            
            // 筛选出不重复的控件
            List<AccessibilityNodeInfo> uniqueNodes = new ArrayList<>();
            for (AccessibilityNodeInfo node : textViewNodes) {
                if (node == null) continue;
                
                String nodeText = node.getText() != null ? node.getText().toString() : "无文本";
                
                // 检查是否已经处理过这个文本
                if (!textList.contains(nodeText)) {
                    uniqueNodes.add(node);
                }
            }
            
            if (uniqueNodes.isEmpty()) {
                Log.w(TAG, "没有找到任何不重复的控件，跳过处理");
                FloatingWindowService.updateService(context, "没有找到任何不重复的控件");
                return;
            }
            
            Log.d(TAG, "筛选出 " + uniqueNodes.size() + " 个不重复的控件（总找到: " + textViewNodes.size() + "）");
            FloatingWindowService.updateService(context, "筛选出 " + uniqueNodes.size() + " 个不重复的控件");
            
            // 使用筛选后的控件列表
            textViewNodes = uniqueNodes;
            FloatingWindowService.updateService(context, "开始处理控件 (1/" + textViewNodes.size() + ")");
            
            // 循环遍历控件，处理每个控件
            for (int nodeIndex = 0; nodeIndex < textViewNodes.size(); nodeIndex++) {
                
                // 检查线程是否被中断
                if (Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "脚本线程被中断，退出控件处理");
                    return;
                }
                
                // 检查脚本是否被停止
                if (!isScriptRunning) {
                    Log.d(TAG, "脚本被停止，退出控件处理");
                    return;
                }
                
                // 检查暂停状态
                FloatingWindowService.waitIfPaused();
                
                // 更新控件处理进度
                FloatingWindowService.updateService(context, "正在处理第 " + (nodeIndex + 1) + "/" + textViewNodes.size() + " 个控件");
                
                // 判断控件是否为空
                AccessibilityNodeInfo textViewNode = textViewNodes.get(nodeIndex);
                if (textViewNode == null) {
                    Log.w(TAG, "第" + (nodeIndex + 1) + "个控件为null，跳过");
                    continue;
                }
                
                // 等待3秒
                AutomationAccessibilityService.Sleep(3000);
                
                // 获取控件文本
                String nodeText = textViewNode.getText() != null ? textViewNode.getText().toString() : "无文本";
                Log.d(TAG, "第" + (nodeIndex + 1) + "/" + textViewNodes.size() + " 控件的文本: " + nodeText);
                FloatingWindowService.updateService(context, "第" + (nodeIndex + 1) + "/" + textViewNodes.size() + " 控件的文本: " + nodeText);
                
                // 获取控件的有效点击坐标（确保在屏幕范围内）
                int[] coordinates = getValidClickCoordinate(nodeText);
                int x = coordinates[0];
                int y = coordinates[1];
                
                Log.d(TAG,"开始点击控件");
                FloatingWindowService.updateService(context, "第" + (nodeIndex + 1) + "/" + textViewNodes.size() + " 控件开始点击");
                
                //获取用户在设置里面设置的控件重复次数
                int clickLoopCount = settingsManager.getClickLoopCount();
                
                // 准备完成，开始执行控件操作
                for (int i = 0; i < clickLoopCount; i++) {
                    // 检查暂停状态
                    FloatingWindowService.waitIfPaused();
                    
                    FloatingWindowService.updateService(context, "第" + (nodeIndex + 1) + "/" + textViewNodes.size() + " 控件 第" + (i + 1) + "/" + clickLoopCount + "次点击");
                    
                    // 使用坐标点击
                    Log.d(TAG, "准备点击坐标: (" + x + ", " + y + ")");
                    boolean clickSuccess = AutomationAccessibilityService.Click(x, y, 100);
                    Log.d(TAG, "坐标点击结果: " + clickSuccess + " 坐标: (" + x + ", " + y + ")");
                    FloatingWindowService.updateService(context, "第" + (nodeIndex + 1) + "/" + textViewNodes.size() + " 控件 第" + (i + 1) + "次点击: " + (clickSuccess ? "成功" : "失败"));
                    
                    //点击控件之后的操作
                    afterClickAction();
                }
                
                Log.d(TAG, "第" + (nodeIndex + 1) + "/" + textViewNodes.size() + " 控件完成，总点击: " + clickLoopCount + "次");
                FloatingWindowService.updateService(context, "第" + (nodeIndex + 1) + "/" + textViewNodes.size() + " 控件完成，总点击: " + clickLoopCount + "次");
                
                //将文本添加到textList中
                textList.add(nodeText);
                processedCount++; // 增加已处理计数
            }
            //判断当前已经执行了控件数目
            int currentControlCount = textList.size();
            FloatingWindowService.updateService(context, "本轮已处理 " + processedCount + " 个控件，总计 " + currentControlCount + " 个");
            //如果当前已经执行了控件数目大于等于最多操作控件数量，则停止查找
            if (currentControlCount >= settingsManager.getMaxControlCount()) {
                Log.d(TAG, "已经执行了 " + currentControlCount + " 个控件，达到最多操作控件数量，停止查找");
                FloatingWindowService.updateService(context, "达到最大控件数量(" + settingsManager.getMaxControlCount() + ")，停止查找");
                return;
            }
            Log.d(TAG, "继续查找新控件");
            FloatingWindowService.updateService(context, "继续查找新控件... (已处理" + currentControlCount + "/" + settingsManager.getMaxControlCount() + ")");
            
            // 检查暂停状态
            FloatingWindowService.waitIfPaused();
            
            CircularSearchElements(textViewNodes, textList);
    }


    /**
     * 点击控件之后执行的操作
     */
    private void afterClickAction() {
        // 检查线程是否被中断
        if (Thread.currentThread().isInterrupted()) {
            Log.d(TAG, "脚本线程被中断，退出点击后操作");
            return;
        }
        
        // 检查脚本是否被停止
        if (!isScriptRunning) {
            Log.d(TAG, "脚本被停止，退出点击后操作");
            return;
        }
        
        // 检查暂停状态
        FloatingWindowService.waitIfPaused();
        
        //等待2秒
        FloatingWindowService.updateService(context, "等待页面加载...");
        AutomationAccessibilityService.Sleep(2000);
        
        // 检查暂停状态
        FloatingWindowService.waitIfPaused();
        
        // 从设置读取用户是否勾选了点击后滑动选项
        boolean swipeAfterClick = settingsManager.isSwipeAfterClickEnabled();
        if (swipeAfterClick) {
            //从配置获取滑动参数
            int swipeStartX = settingsManager.getSwipeStartX();
            int swipeStartY = settingsManager.getSwipeStartY();
            int swipeEndX = settingsManager.getSwipeEndX();
            int swipeEndY = settingsManager.getSwipeEndY();
            int swipeDuration = settingsManager.getSwipeDuration();
            //滑动
            FloatingWindowService.updateService(context, "执行滑动操作...");
            AutomationAccessibilityService.Swipe(swipeStartX, swipeStartY, swipeEndX, swipeEndY, swipeDuration);
            AutomationAccessibilityService.Sleep(swipeDuration+2000);
            
            // 滑动完成后立即检查脚本是否被停止
            if (!isScriptRunning) {
                Log.d(TAG, "脚本被停止，退出滑动后操作");
                return;
            }
            
            // 检查暂停状态
            FloatingWindowService.waitIfPaused();
        } else {
            Log.d(TAG, "用户已禁用点击后滑动，跳过滑动操作");
        }
        
        //从设置读取用户是否勾选了点击商品链接选项
        boolean clickProductLink = settingsManager.isClickProductLinkEnabled();
        if (clickProductLink) {
            try{
                //点击商品链接
                FloatingWindowService.updateService(context, "查找商品链接...");
                List<AccessibilityNodeInfo> allElements = AutomationAccessibilityService.GetAllElements();
                List<AccessibilityNodeInfo> productElements = AutomationAccessibilityService.findElementListById(allElements, "com.shizhuang.duapp:id/containerProduct");
                
                if (productElements != null && !productElements.isEmpty()) {
                    AccessibilityNodeInfo productElement = productElements.get(0);
                    
                    // 获取控件的坐标
                    Rect bounds = new Rect();
                    productElement.getBoundsInScreen(bounds);
                    int x = (bounds.left + bounds.right) / 2;
                    int y = (bounds.top + bounds.bottom) / 2;
                    
                    FloatingWindowService.updateService(context, "点击商品链接 (" + x + ", " + y + ")...");
                    boolean clickSuccess = AutomationAccessibilityService.Click(x, y, 100);
                    Log.d(TAG, "商品链接点击结果: " + clickSuccess + " 坐标: (" + x + ", " + y + ")");
                    FloatingWindowService.updateService(context, "商品链接点击: " + (clickSuccess ? "成功" : "失败"));
                    AutomationAccessibilityService.Sleep(1000);
                    
                    //返回
                    FloatingWindowService.updateService(context, "返回上一页...");
                    AutomationAccessibilityService.GoBack();
                    AutomationAccessibilityService.Sleep(1000);
                } else {
                    Log.w(TAG, "未找到商品链接控件");
                    FloatingWindowService.updateService(context, "未找到商品链接控件");
                }
            }catch(Exception e){
                Log.e(TAG, "点击商品链接时出错", e);
                FloatingWindowService.updateService(context, "点击商品链接失败: " + e.getMessage());
            }
        }
        
        // 检查脚本是否被停止
        if (!isScriptRunning) {
            Log.d(TAG, "脚本被停止，退出返回操作");
            return;
        }
        
        // 检查暂停状态
        FloatingWindowService.waitIfPaused();
        
        FloatingWindowService.updateService(context, "返回主页面...");
        AutomationAccessibilityService.GoBack();
        AutomationAccessibilityService.Sleep(1500);

    }

    /**
     * 获取控件的有效点击坐标，确保坐标在屏幕范围内
     * 如果控件不在屏幕有效区域内，会自动滚动屏幕直到控件可见
     * @param nodeText 目标控件的文本，用于重新查找控件
     * @return 包含x和y坐标的数组，[0]=x坐标，[1]=y坐标
     */
    private int[] getValidClickCoordinate(String nodeText) {
        // 获取屏幕尺寸
        android.util.DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        int threshold = 150; // 临界值150像素
        
        // 记录上一次的坐标，用于判断是否已经滚动到底
        int lastX = -1;
        int lastY = -1;
        
        // 循环滚动直到控件在有效区域内
        while (true) {
            // 重新获取所有元素
            List<AccessibilityNodeInfo> allElements = AutomationAccessibilityService.GetAllElements();
            if (allElements == null || allElements.isEmpty()) {
                Log.w(TAG, "无法获取页面元素");
                return new int[]{-1, -1};
            }
            
            // 查找包含指定文本的控件
            AccessibilityNodeInfo targetNode = null;
            List<AccessibilityNodeInfo> textViewNodes = AutomationAccessibilityService.findElementListById(allElements, "com.shizhuang.duapp:id/tvTitle");
            if (textViewNodes != null) {
                for (AccessibilityNodeInfo node : textViewNodes) {
                    if (node == null) continue;
                    String text = node.getText() != null ? node.getText().toString() : "";
                    if (text.equals(nodeText)) {
                        targetNode = node;
                        break;
                    }
                }
            }
            
            if (targetNode == null) {
                Log.e(TAG, "无法找到目标控件: " + nodeText);
                return new int[]{-1, -1};
            }
            
            // 获取控件的当前坐标
            Rect bounds = new Rect();
            targetNode.getBoundsInScreen(bounds);
            int x = (bounds.left + bounds.right) / 2;
            int y = (bounds.top + bounds.bottom) / 2;
            Log.d(TAG, "控件的坐标: " + x + ", " + y);
            
            // 检查控件是否在有效点击区域内（考虑临界值）
            boolean isInValidArea = (x >= threshold && x <= screenWidth - threshold && 
                                   y >= threshold && y <= screenHeight - threshold);
            
            if (isInValidArea) {
                Log.d(TAG, "控件在有效点击区域内: (" + x + ", " + y + "), 屏幕尺寸: " + screenWidth + "x" + screenHeight + ", 临界值: " + threshold);
                FloatingWindowService.updateService(context, "控件在有效区域内，准备点击");
                return new int[]{x, y};
            }
            
            // 检查坐标是否变化，如果没变化说明已经滚动到底了
            if (lastX == x && lastY == y) {
                Log.d(TAG, "坐标未变化，说明已滚动到底，返回当前坐标: (" + x + ", " + y + ")");
                FloatingWindowService.updateService(context, "已滚动到底，使用当前坐标");
                return new int[]{x, y};
            }
            
            // 更新上一次的坐标
            lastX = x;
            lastY = y;
            
            // 控件不在有效区域内，需要滚动
            Log.w(TAG, "控件的坐标不在有效点击区域内: (" + x + ", " + y + "), 屏幕尺寸: " + screenWidth + "x" + screenHeight + ", 临界值: " + threshold);
            FloatingWindowService.updateService(context, "控件不在有效区域，滚动中...");
            
            //计算滚动距离 - 主要针对Y轴滚动
            int scrollDistance;
            if (y > screenHeight - threshold) {
                // 控件在屏幕下方，需要向上滚动
                scrollDistance = y - (screenHeight - threshold);
            } else if (y < threshold) {
                // 控件在屏幕上方，需要向下滚动（这种情况较少见）
                scrollDistance = threshold - y;
            } else {
                // 其他情况，默认滚动
                scrollDistance = y - screenHeight;
            }
            
            // 如果滚动距离小于50，固定为150（保持原方向）
            if (Math.abs(scrollDistance) < 50) {
                scrollDistance = scrollDistance >= 0 ? 180 : -180;
            }
            
            Log.d(TAG, "滚动距离: " + scrollDistance);
            
            //滚动屏幕
            AutomationAccessibilityService.Swipe(500, screenHeight-400, 500, screenHeight-scrollDistance-400, 2000);
            Log.d(TAG, "等待滚动完成");
            AutomationAccessibilityService.Sleep(2500);
            
            // 检查线程是否被中断或脚本是否被停止
            if (Thread.currentThread().isInterrupted() || !isScriptRunning) {
                Log.d(TAG, "脚本被停止，返回最后坐标");
                return new int[]{x, y};
            }
            
            // 等待1秒后，继续下一次循环，重新获取所有元素和控件坐标
        }
    }

    /**
     * 释放资源
     * 关闭线程池和释放截图助手资源，防止内存泄漏
     */
    public void release() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    /**
     * 测试停止功能
     */
    public void testStopFunction() {
        Log.d(TAG, "测试停止功能 - 当前运行状态: " + isScriptRunning);
        Log.d(TAG, "测试停止功能 - 当前回调: " + (currentCallback != null ? "存在" : "null"));
    }

}
