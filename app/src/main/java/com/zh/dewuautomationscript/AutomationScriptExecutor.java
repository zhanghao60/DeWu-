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
    
    // TAG标签名字
    private static final String TAG = "AutomationScriptExecutor";
    // Android上下文对象
    private Context context;
    // json
    private JsonFileManager jsonFileManager;
    // http客户端
    private OkHttpClient httpClient;
    // 脚本执行
    private ExecutorService executorService;
    // hander
    private Handler mainHandler;
    // 无障碍服务操作类
    private AutomationAccessibilityService automationService;
    // 设置
    private SettingsManager settingsManager;
    // 是否启用脚本
    private volatile boolean isScriptRunning = false;
    // 回调
    private ScriptCallback currentCallback;
    // 线程
    private volatile Thread scriptThread;
    // 静态引用，用于在Sleep等方法中检查脚本运行状态
    private static volatile AutomationScriptExecutor currentInstance = null;
    
    // 回调接口，用于更新UI信息
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
            // 初始化上下文
            this.context = context;
            // 初始化JSON文件管理器
            this.jsonFileManager = new JsonFileManager(context);
            // 初始化HTTP客户端
            this.httpClient = new OkHttpClient();
            // 初始化线程池
            this.executorService = Executors.newSingleThreadExecutor();
            // 初始化主线程处理器
            this.mainHandler = new Handler(Looper.getMainLooper());
            // 初始化无障碍服务操作类
            this.automationService = AutomationAccessibilityService.getInstance();
            // 初始化设置管理器
            this.settingsManager = new SettingsManager(context);
            // 设置当前实例，用于在Sleep等方法中检查脚本运行状态
            currentInstance = this;
        } catch (Exception e) {
            Log.e(TAG, "AutomationScriptExecutor初始化失败", e);
            throw new RuntimeException("初始化失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查脚本是否正在运行（静态方法，供Sleep等方法调用）
     */
    public static boolean isScriptRunningStatic() {
        AutomationScriptExecutor instance = currentInstance;
        return instance != null && instance.isScriptRunning;
    }
    

    /**
     * 处理得物深度链接
     * 直接打开得物app并执行自动化操作，从JSON文件读取数据
     * @param url 得物深度链接（已废弃，保留用于兼容）
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
            // 打印日志，开始处理得物app自动化操作
            Log.d(TAG, "开始得物app自动化操作");
            
            // 更新悬浮窗状态
            // 读取JSON文件
            FloatingWindowService.updateService(context, "正在读取JSON文件...");
            
            // 1. 从JSON文件读取URL列表
            List<UrlItem> urlList = jsonFileManager.loadUrlsFromFile();
            if (urlList.isEmpty()) {
                Log.w(TAG, "JSON文件中没有找到数据");
                mainHandler.post(() -> callback.onError("JSON文件中没有找到数据，请先添加URL"));
                return;
            }
            
            Log.d(TAG, "从JSON文件读取到 " + urlList.size() + " 条数据");

            //打印读取到的标题和发布人
            for (UrlItem urlItem : urlList) {
                Log.d(TAG, "标题: " + urlItem.getTitle() + " 发布人: " + urlItem.getPublisher());
            }
            // 更新悬浮窗状态为打开得物app
            FloatingWindowService.updateService(context, "正在打开得物app...");
            
            // 2. 只打开得物app，不进行intent跳转
            boolean openSuccess = openDewuApp();
            if (!openSuccess) {
                Log.e(TAG, "打开得物app失败");
                mainHandler.post(() -> callback.onError("无法打开得物app，请确保已安装得物应用"));
                return;
            }
            
            Log.d(TAG, "得物app打开成功");
            FloatingWindowService.updateService(context, "得物app打开成功");
            
            // 3. 等待得物app加载完成
            int dewuAppWaitTime = settingsManager.getDewuAppWaitTime();
            Log.d(TAG, "等待得物app加载" + (dewuAppWaitTime / 1000) + "秒...");
            
            // 更新悬浮窗状态
            FloatingWindowService.updateService(context, "等待得物app加载...");
            
            // Sleep 方法内部会自动检查暂停状态，无需在此处额外检查
            automationService.Sleep(dewuAppWaitTime);
            
            Log.d(TAG, "得物app加载完成，开始逐条处理URL");
            FloatingWindowService.updateService(context, "得物app加载完成，开始逐条处理URL...");

            // 循环：用于逐条处理URL列表，直到处理完所有URL
            for (int i = 0; i < urlList.size(); i++) {
                // 检查线程/运行状态
                if (Thread.currentThread().isInterrupted() || !isScriptRunning) {
                    Log.d(TAG, "脚本被停止或线程中断，提前结束URL处理");
                    break;
                }

                UrlItem item = urlList.get(i);
                Log.d(TAG, "开始处理第 " + (i + 1) + "/" + urlList.size() + " 条: 标题=" + item.getTitle() + " 发布人=" + item.getPublisher());

                // 更新悬浮窗状态并检查暂停
                FloatingWindowService.updateService(context, "正在搜索商品，寻找主页，标题: " + item.getTitle());
                FloatingWindowService.waitIfPaused();

                FloatingWindowService.updateService(context, "点击得物主页的tabar");
                // 点击得物主页的tabar
                ClickHomePageTabar();
                FloatingWindowService.updateService(context, "已经点进得物按钮");
                AutomationAccessibilityService.Sleep(1000);
                // 搜索商品，点进主页
                FloatingWindowService.updateService(context, "搜索商品");
                SearchProduct(item.getTitle(), item.getPublisher());

                // 查找并操作主页当中的控件（SearchProduct 内部的 Sleep 会自动检查暂停状态）
                String targetFullId = "com.shizhuang.duapp:id/imgPhoto";
                Log.d(TAG, "开始查找并操作主页的目标控件: " + targetFullId);
                FloatingWindowService.updateService(context, "查找控件中...");
                findAndOperateControls(targetFullId);

                // 回调当前URL处理完成
                String callbackUrl = item.getUrl();
                final int index = i;
                mainHandler.post(() -> {
                    callback.onClickPerformed(callbackUrl, -1, -1, true);
                    callback.onUrlProcessed(index + 1, urlList.size(), callbackUrl);
                });

                // 处理完一个URL后，清理并重启得物app（若还有后续URL）
                if (i < urlList.size() - 1) {
                    Log.d(TAG, "准备清理并重启得物app...");
                    FloatingWindowService.updateService(context, "重启得物app...");
                    resetAndReopenDewu(dewuAppWaitTime);
                }
            }

            Log.d(TAG, "得物app自动化操作完成（全部URL处理结束或被中断）");
            
        } catch (Exception e) {
            Log.e(TAG, "处理得物app自动化操作失败", e);
            mainHandler.post(() -> callback.onError("处理得物app自动化操作失败: " + e.getMessage()));
        }
    }
    
    
    /**
     * 点击得物主页的tabar（点击id为com.shizhuang.duapp:id/tab_trends的节点）
     */
    private void ClickHomePageTabar() {
        try {
            Log.d(TAG, "开始点击得物主页的tabar");
            
            // 获取所有控件
            List<AccessibilityNodeInfo> allElements = AutomationAccessibilityService.GetAllElements();
            if (allElements == null || allElements.isEmpty()) {
                Log.w(TAG, "无法获取页面元素，可能页面未加载完成");
                FloatingWindowService.updateService(context, "无法获取页面元素，可能页面未加载完成");
                return;
            }
            
            // 查找id为com.shizhuang.duapp:id/tab_trends的节点
            List<AccessibilityNodeInfo> tabarNodes = AutomationAccessibilityService.findElementListById(allElements, "com.shizhuang.duapp:id/tab_trends");
            if (tabarNodes != null && !tabarNodes.isEmpty()) {
                AccessibilityNodeInfo tabarNode = tabarNodes.get(0);
                Log.d(TAG, "找到id为com.shizhuang.duapp:id/tab_trends的节点");
                FloatingWindowService.updateService(context, "找到主页tabar节点");
                
                // 获取节点坐标
                Rect bounds = new Rect();
                tabarNode.getBoundsInScreen(bounds);
                int x = (bounds.left + bounds.right) / 2;
                int y = (bounds.top + bounds.bottom) / 2;
                
                // 点击节点
                Log.d(TAG, "点击主页tabar，坐标: (" + x + ", " + y + ")");
                FloatingWindowService.updateService(context, "点击主页tabar，坐标: (" + x + ", " + y + ")");
                AutomationAccessibilityService.Click(x, y, 100);
                
                // 等待页面加载
                AutomationAccessibilityService.Sleep(1000);
            } else {
                Log.w(TAG, "未找到id为com.shizhuang.duapp:id/tab_trends的节点");
                FloatingWindowService.updateService(context, "未找到主页tabar节点");
            }
        } catch (Exception e) {
            Log.e(TAG, "点击主页tabar失败", e);
            FloatingWindowService.updateService(context, "点击主页tabar失败: " + e.getMessage());
        }
    }
    
    
    /**
     * @param title 商品标题
     * @param publisher 商品发布人
     */
    private void SearchProduct(String title, String publisher) {
        try {
            // 通过商品搜索用户主页
            Log.d(TAG, "开始搜索商品，寻找搜索框");
            FloatingWindowService.updateService(context, "开始搜索商品，寻找搜索框");
            // 获取所有控件
            List<AccessibilityNodeInfo> allElements = AutomationAccessibilityService.GetAllElements();
            if (allElements == null || allElements.isEmpty()) {
                Log.w(TAG, "无法获取页面元素，可能页面未加载完成");
                FloatingWindowService.updateService(context, "无法获取页面元素，可能页面未加载完成");
                return;
            }
            
            // 使用已有的方法查找搜索框节点
            List<AccessibilityNodeInfo> searchNodes = AutomationAccessibilityService.findElementListById(allElements, "com.shizhuang.duapp:id/flSearchB");
            if (searchNodes != null && !searchNodes.isEmpty()) {
                AccessibilityNodeInfo searchNode = searchNodes.get(0);
                Log.d(TAG, "找到id为com.shizhuang.duapp:id/flSearchB的节点");
                FloatingWindowService.updateService(context, "找到搜索框节点");
                // 获取节点坐标
                Rect bounds = new Rect();
                searchNode.getBoundsInScreen(bounds);
                int x = (bounds.left + bounds.right) / 2;
                int y = (bounds.top + bounds.bottom) / 2;
                // 点击节点
                Log.d(TAG, "点击搜索框，坐标: (" + x + ", " + y + ")");
                FloatingWindowService.updateService(context, "点击搜索框，坐标: (" + x + ", " + y + ")");
                AutomationAccessibilityService.Click(x, y, 100);
                AutomationAccessibilityService.Sleep(200);
            } else {
                Log.w(TAG, "未找到搜索框节点");
            }
            // 等待一秒，准备输入搜索内容，开始输入商品标题
            FloatingWindowService.updateService(context, "等待一秒，准备输入搜索内容，开始输入商品标题");
            AutomationAccessibilityService.Sleep(1000);
            // 获取所有节点
            allElements = AutomationAccessibilityService.GetAllElements();
            if (allElements == null || allElements.isEmpty()) {
                Log.w(TAG, "无法获取页面元素，可能页面未加载完成");
                FloatingWindowService.updateService(context, "无法获取页面元素，可能页面未加载完成");
                return;
            }
            // 获取输入框节点
            List<AccessibilityNodeInfo> inputNodes = AutomationAccessibilityService.findElementListById(allElements, "com.shizhuang.duapp:id/etSearch");
            if (inputNodes != null && !inputNodes.isEmpty()) {
                AccessibilityNodeInfo inputNode = inputNodes.get(0);
                Log.d(TAG, "找到id为com.shizhuang.duapp:id/etSearch的节点");
                // 获取节点坐标
                Rect bounds = new Rect();
                inputNode.getBoundsInScreen(bounds);
                int x = (bounds.left + bounds.right) / 2;
                int y = (bounds.top + bounds.bottom) / 2;
                // 点击节点
                Log.d(TAG, "点击输入框，坐标: (" + x + ", " + y + ")");
                AutomationAccessibilityService.Click(x, y, 100);
                AutomationAccessibilityService.Sleep(200);
                // 输入商品标题
                Log.d(TAG, "输入商品标题: " + title);
                AutomationAccessibilityService.InputText(title, inputNode);
                AutomationAccessibilityService.Sleep(200);

            } else {
                Log.w(TAG, "未找到输入框节点");
            }
            // 等待一秒，准备点击搜索按钮，开始搜索商品
            FloatingWindowService.updateService(context, "等待一秒，准备点击搜索按钮，开始搜索商品");
            AutomationAccessibilityService.Sleep(1000);
            // 获取所有节点
            allElements = AutomationAccessibilityService.GetAllElements();
            if (allElements == null || allElements.isEmpty()) {
                Log.w(TAG, "无法获取页面元素，可能页面未加载完成");
                return;
            }
            // 获取搜索按钮节点
            List<AccessibilityNodeInfo> searchButtonNodes = AutomationAccessibilityService.findElementListById(allElements, "com.shizhuang.duapp:id/tvSearch");
            if (searchButtonNodes != null && !searchButtonNodes.isEmpty()) {
                AccessibilityNodeInfo searchButtonNode = searchButtonNodes.get(0);
                Log.d(TAG, "找到id为com.shizhuang.duapp:id/tvSearch的节点");
                // 获取节点坐标
                Rect bounds = new Rect();
                searchButtonNode.getBoundsInScreen(bounds);
                int x = (bounds.left + bounds.right) / 2;
                int y = (bounds.top + bounds.bottom) / 2;
                // 点击节点
                Log.d(TAG, "点击搜索节点，坐标: (" + x + ", " + y + ")");
                AutomationAccessibilityService.Click(x, y, 100);
                // 等待5秒
                AutomationAccessibilityService.Sleep(5000);
                // 执行滚动屏幕，直到找到符合条件的商品
                ScrollScreen(title, publisher);
            } else {
                Log.w(TAG, "未找到搜索节点");
            }
        } catch (Exception e) {
            Log.e(TAG, "搜索商品失败", e);
        }
    }


    /**
     * 打开得物app
     * 直接启动得物应用主界面，不进行intent跳转
     * @return true=成功打开，false=打开失败
     */
    private boolean openDewuApp() {
        try {
            // 获取得物app的启动Intent（主界面）
            android.content.Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.shizhuang.duapp");
            
            if (intent != null) {
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
                Log.d(TAG, "成功打开得物app主界面");
                return true;
            } else {
                Log.e(TAG, "得物app未安装或无法启动");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "打开得物app失败", e);
            return false;
        }
    }

    /**
     * 返回桌面并尽量清理得物后台，然后重新打开得物并等待加载
     * @param waitMs 等待得物加载时间
     */
    private void resetAndReopenDewu(int waitMs) {
        try {
            // 返回桌面
            android.content.Intent homeIntent = new android.content.Intent(android.content.Intent.ACTION_MAIN);
            homeIntent.addCategory(android.content.Intent.CATEGORY_HOME);
            homeIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(homeIntent);
        } catch (Exception e) {
            Log.e(TAG, "返回桌面失败", e);
        }

        // 等待片刻
        AutomationAccessibilityService.Sleep(800);

        // 尝试清理得物后台进程（需要权限，失败则忽略）
        try {
            android.app.ActivityManager am = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                am.killBackgroundProcesses("com.shizhuang.duapp");
            }
        } catch (Exception e) {
            Log.w(TAG, "清理得物后台进程失败或无权限", e);
        }

        // 再次打开得物主界面
        openDewuApp();

        // 等待得物加载
        AutomationAccessibilityService.Sleep(waitMs);
    }

    /**
     * 停止脚本执行
     */
    public void stopScript() {
        Log.d(TAG, "停止脚本执行");
        isScriptRunning = false;
        
        // 唤醒可能正在等待暂停恢复的线程
        FloatingWindowService.setPaused(false);
        
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
                
                // 2. 执行得物app自动化操作（从JSON文件读取数据）
                Log.d(TAG, "开始执行得物app自动化操作");
                processDewuDeepLink("", callback, true);
                
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
            
            // 过滤出层级大于等于18的节点
            List<AccessibilityNodeInfo> filteredNodes = new ArrayList<>();
            for (AccessibilityNodeInfo node : textViewNodes) {
                if (node != null) {
                    int depth = getNodeDepth(node);
                    String nodeText = node.getText() != null ? node.getText().toString() : "无文本";
                    Log.d(TAG, "节点文本: " + nodeText + ", 层级深度: " + depth);
                    if (depth >= 18) {
                        filteredNodes.add(node);
                    }
                }
            }
            textViewNodes = filteredNodes;
            
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
                
                // 检查坐标是否有效
                if (x < 0 || y < 0) {
                    Log.w(TAG, "无法获取有效坐标，跳过控件: " + nodeText + ", 坐标: (" + x + ", " + y + ")");
                    FloatingWindowService.updateService(context, "无法获取有效坐标，跳过控件: " + nodeText);
                    // 仍然添加到已处理列表，避免重复处理
                    textList.add(nodeText);
                    continue;
                }
                
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
        
        //等待2秒（Sleep 方法内部会自动检查暂停状态）
        FloatingWindowService.updateService(context, "等待页面加载...");
        AutomationAccessibilityService.Sleep(2000);
        
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
            // Sleep 方法内部会自动检查暂停状态
            AutomationAccessibilityService.Sleep(swipeDuration+2000);
            
            // 滑动完成后立即检查脚本是否被停止
            if (!isScriptRunning) {
                Log.d(TAG, "脚本被停止，退出滑动后操作");
                return;
            }
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
        
        FloatingWindowService.updateService(context, "返回主页面...");
        AutomationAccessibilityService.GoBack();
        // Sleep 方法内部会自动检查暂停状态
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
                // 过滤出层级大于等于18的节点
                List<AccessibilityNodeInfo> filteredNodes = new ArrayList<>();
                for (AccessibilityNodeInfo node : textViewNodes) {
                    if (node != null && getNodeDepth(node) >= 18) {
                        filteredNodes.add(node);
                    }
                }
                // 在过滤后的节点中查找匹配文本的节点
                for (AccessibilityNodeInfo node : filteredNodes) {
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
            
            // 获取用户主页滑动参数
            int userHomePageSwipeStartX = settingsManager.getUserHomePageSwipeStartX();
            int userHomePageSwipeStartY = settingsManager.getUserHomePageSwipeStartY();
            int userHomePageSwipeEndX = settingsManager.getUserHomePageSwipeEndX();
            int userHomePageSwipeEndYOffset = settingsManager.getUserHomePageSwipeEndYOffset();
            int userHomePageSwipeDuration = settingsManager.getUserHomePageSwipeDuration();
            
            // 计算起始坐标（负数表示从底部偏移）
            int startX = userHomePageSwipeStartX;
            int startY = (userHomePageSwipeStartY <= 0) ? (screenHeight + userHomePageSwipeStartY) : userHomePageSwipeStartY;
            
            // 计算结束坐标（结束Y由代码动态计算：screenHeight + 偏移量 - scrollDistance）
            int endX = userHomePageSwipeEndX;
            int endY = screenHeight + userHomePageSwipeEndYOffset - scrollDistance;
            
            //滚动屏幕
            Log.d(TAG, "用户主页滑动: (" + startX + ", " + startY + ") -> (" + endX + ", " + endY + "), 持续时间: " + userHomePageSwipeDuration + "ms");
            AutomationAccessibilityService.Swipe(startX, startY, endX, endY, userHomePageSwipeDuration);
            Log.d(TAG, "等待滚动完成");
            AutomationAccessibilityService.Sleep(2500);
            
            // 检查线程是否被中断或脚本是否被停止
            if (Thread.currentThread().isInterrupted() || !isScriptRunning) {
                Log.d(TAG, "脚本被停止，返回最后坐标");
                return new int[]{x, y};
            }
            
            // 继续下一次循环，重新获取所有元素和控件坐标
        }
    }

    /**
     * 释放资源
     * 关闭线程池和释放截图助手资源，防止内存泄漏
     */
    public void release() {
        // 清除当前实例引用
        if (currentInstance == this) {
            currentInstance = null;
        }
        
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
    

    /**
     * 滚动屏幕，直到找到符合条件的商品,然后点进对应的主页面
     * @param title 商品标题
     * @param publisher 商品发布人
     * @return 是否找到符合条件的商品
     */
    private boolean ScrollScreen(String title, String publisher) {
        try {
            // 循环：用于滚动屏幕，直到找到符合条件的商品
            while (true) {
                // 检查脚本是否被停止
                if (!isScriptRunning || Thread.currentThread().isInterrupted()) {
                    Log.d(TAG, "脚本被停止或线程中断，退出滚动屏幕");
                    return false;
                }
                
                // 获取屏幕尺寸
                android.util.DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                int screenWidth = metrics.widthPixels;
                int screenHeight = metrics.heightPixels;
                // 初始化滚动距离
                int scrollDistance = 0; // 初始化滚动距离，默认为0
                // 获取所有元素
                List<AccessibilityNodeInfo> allElements = AutomationAccessibilityService.GetAllElements();
                if (allElements == null || allElements.isEmpty()) {
                    Log.w(TAG, "无法获取页面元素，可能页面未加载完成");
                    return false;
                }
                // 获取id为:com.shizhuang.duapp:id/tvUsername的TextView节点，用于匹配发布人
                List<AccessibilityNodeInfo> usernameNodes = AutomationAccessibilityService.findElementListById(allElements, "com.shizhuang.duapp:id/tvUsername");
                if (usernameNodes != null && !usernameNodes.isEmpty()) {
                    // 循环：用于遍历所有的节点，直到找到包含发布人的TextView节点
                    for (AccessibilityNodeInfo usernameNode : usernameNodes) {
                        Log.d(TAG, "找到id为com.shizhuang.duapp:id/tvUsername的节点");
                        FloatingWindowService.updateService(context, "找到包含发布人的TextView节点");
                        // 获取节点文本
                        String username = usernameNode.getText() != null ? usernameNode.getText().toString() : "";
                        Log.d(TAG, "节点文本: " + username);
                        //将节点文本和publisher进行匹配
                        if (username.equals(publisher)) {
                            Log.d(TAG, "节点文本和publisher匹配成功");
                        } else {
                            Log.d(TAG, "节点文本和publisher匹配失败");
                            continue;
                        }
                        // 获取节点坐标
                        Rect bounds = new Rect();
                        usernameNode.getBoundsInScreen(bounds);
                        int x = (bounds.left + bounds.right) / 2;
                        int y = (bounds.top + bounds.bottom) / 2;
                        Log.d(TAG, "节点坐标: " + x + ", " + y);
                        // 判断坐标是否在屏幕范围内
                        if (x >= 0 && x <= screenWidth && y >= 0 && y <= screenHeight) {
                            Log.d(TAG, "节点坐标在屏幕范围内，可以直接点击");
                            // 点击节点，坐标点击
                            AutomationAccessibilityService.Click(x, y, 100);
                            // 等待2秒,等待进入商品详情页面
                            AutomationAccessibilityService.Sleep(2000);
                            // 获取所有元素，用于匹配用户按钮
                            allElements = AutomationAccessibilityService.GetAllElements();
                            if (allElements == null || allElements.isEmpty()) {
                                Log.w(TAG, "无法获取页面元素，可能页面未加载完成");
                                return false;
                            }
                            // 获取id为com.shizhuang.duapp:id/tvUsername的TextView节点
                            usernameNodes = AutomationAccessibilityService.findElementListById(allElements, "com.shizhuang.duapp:id/tvUsername");
                            if (usernameNodes != null && !usernameNodes.isEmpty()) {
                                for (AccessibilityNodeInfo confirmNode : usernameNodes) {
                                    if (confirmNode == null) continue;
                                    String confirmUsername = confirmNode.getText() != null ? confirmNode.getText().toString() : "";
                                    if (confirmUsername.equals(publisher)) {
                                        Log.d(TAG, "找到用户按钮节点，点击用户按钮");
                                        // 获取节点坐标
                                        bounds = new Rect();
                                        confirmNode.getBoundsInScreen(bounds);
                                        x = (bounds.left + bounds.right) / 2;
                                        y = (bounds.top + bounds.bottom) / 2;
                                        Log.d(TAG, "节点坐标: " + x + ", " + y);
                                        // 点击节点，坐标点击
                                        AutomationAccessibilityService.Click(x, y, 100);
                                        // 从配置当中获取等待时间,等待进入用户主页
                                        int enterHomePageWaitTime = settingsManager.getEnterHomePageWaitTime();
                                        AutomationAccessibilityService.Sleep(enterHomePageWaitTime);
                                        //返回true，退出函数
                                        return true;
                                    }
                                }
                            }
                            else {
                                Log.w(TAG, "未找到id为com.shizhuang.duapp:id/tvUsername的节点");
                            }
                        } 
                        else {
                            Log.d(TAG, "节点坐标不在屏幕范围内,需要滚动屏幕,直到节点在屏幕范围内");
                            while (true) {
                                // 检查脚本是否被停止
                                if (!isScriptRunning || Thread.currentThread().isInterrupted()) {
                                    Log.d(TAG, "脚本被停止或线程中断，退出内部滚动循环");
                                    return false;
                                }
                                
                                // 获取所有元素
                                allElements = AutomationAccessibilityService.GetAllElements();
                                if (allElements == null || allElements.isEmpty()) {
                                    Log.w(TAG, "无法获取页面元素，可能页面未加载完成");
                                    return false;
                                }
                                // 获取id为com.shizhuang.duapp:id/tvUsername的TextView节点
                                usernameNodes = AutomationAccessibilityService.findElementListById(allElements, "com.shizhuang.duapp:id/tvUsername");
                                if (usernameNodes != null && !usernameNodes.isEmpty()) {
                                    // 获取特定的TextNode节点
                                    for (AccessibilityNodeInfo candidateNode : usernameNodes) {
                                        if (candidateNode == null) continue;
                                        String candidateUsername = candidateNode.getText() != null ? candidateNode.getText().toString() : "";
                                        if (candidateUsername.equals(publisher)) {
                                            Log.d(TAG, "节点文本和publisher匹配成功");
                                            // 获取节点坐标
                                            bounds = new Rect();
                                            candidateNode.getBoundsInScreen(bounds);
                                            x = (bounds.left + bounds.right) / 2;
                                            y = (bounds.top + bounds.bottom) / 2;
                                            Log.d(TAG, "节点坐标: " + x + ", " + y);
                                            // 如果在屏幕内，则跳出循环
                                            if (x >= 0 && x <= screenWidth && y >= 0 && y <= screenHeight) {
                                                Log.d(TAG, "节点坐标在屏幕范围内，执行对应动作,跳出循环");
                                                // 点击节点，坐标点击
                                                AutomationAccessibilityService.Click(x, y, 100);
                                                // 等待1秒
                                                AutomationAccessibilityService.Sleep(1000);
                                                //获取所有元素
                                                allElements = AutomationAccessibilityService.GetAllElements();
                                                if (allElements == null || allElements.isEmpty()) {
                                                    Log.w(TAG, "无法获取页面元素，可能页面未加载完成");
                                                    return false;
                                                }
                                                // 获取id为com.shizhuang.duapp:id/tvUsername的TextView节点
                                                usernameNodes = AutomationAccessibilityService.findElementListById(allElements, "com.shizhuang.duapp:id/tvUsername");
                                                if (usernameNodes != null && !usernameNodes.isEmpty()) {
                                                    for (AccessibilityNodeInfo confirmNode : usernameNodes) {
                                                        if (confirmNode == null) continue;
                                                        String confirmUsername = confirmNode.getText() != null ? confirmNode.getText().toString() : "";
                                                        if (confirmUsername.equals(publisher)) {
                                                            Log.d(TAG, "节点文本和publisher匹配成功");
                                                            // 获取节点坐标
                                                            bounds = new Rect();
                                                            confirmNode.getBoundsInScreen(bounds);
                                                            x = (bounds.left + bounds.right) / 2;
                                                            y = (bounds.top + bounds.bottom) / 2;
                                                            Log.d(TAG, "节点坐标: " + x + ", " + y);
                                                            // 点击节点，坐标点击
                                                            AutomationAccessibilityService.Click(x, y, 100);
                                                            // 从配置当中获取等待时间,等待进入用户主页
                                                            int enterHomePageWaitTime = settingsManager.getEnterHomePageWaitTime();
                                                            AutomationAccessibilityService.Sleep(enterHomePageWaitTime);
                                                            return true;
                                                        }
                                                    }
                                                }
                                                else {
                                                    Log.w(TAG, "未找到id为com.shizhuang.duapp:id/tvUsername的节点");
                                                }
                                                return true;
                                            }
                                            // 如果不在屏幕范围内，则滚动屏幕
                                            // 获取搜索商品时滑动参数
                                            int searchProductSwipeStartX = settingsManager.getSearchProductSwipeStartX();
                                            int searchProductSwipeStartY = settingsManager.getSearchProductSwipeStartY();
                                            int searchProductSwipeEndX = settingsManager.getSearchProductSwipeEndX();
                                            int searchProductSwipeEndYOffset = settingsManager.getSearchProductSwipeEndYOffset();
                                            int searchProductSwipeDuration = settingsManager.getSearchProductSwipeDuration();
                                            
                                            // 计算起始坐标（0表示屏幕中心，负数表示从底部偏移）
                                            int startX = (searchProductSwipeStartX == 0) ? (screenWidth / 2) : searchProductSwipeStartX;
                                            int startY = (searchProductSwipeStartY <= 0) ? (screenHeight + searchProductSwipeStartY) : searchProductSwipeStartY;
                                            
                                            // 计算结束坐标
                                            int endX = (searchProductSwipeEndX == 0) ? (screenWidth / 2) : searchProductSwipeEndX;
                                            
                                            // 使用设置中的偏移量计算滚动阈值
                                            int threshold = (searchProductSwipeStartY <= 0) ? Math.abs(searchProductSwipeStartY) : (screenHeight - searchProductSwipeStartY);
                                            
                                            if (y > screenHeight - threshold) {
                                                Log.d(TAG, "节点坐标在屏幕下方，需要向上滚动");
                                                scrollDistance = y - (screenHeight - threshold);
                                            } else if (y < threshold) {
                                                Log.d(TAG, "节点坐标在屏幕上方，需要向下滚动");
                                                scrollDistance = threshold - y;
                                            }
                                            
                                            // 计算结束Y坐标（screenHeight + 偏移量 - scrollDistance）
                                            int endY = screenHeight + searchProductSwipeEndYOffset - scrollDistance;
                                            
                                            // 滚动屏幕
                                            Log.d(TAG, "搜索商品时滑动: (" + startX + ", " + startY + ") -> (" + endX + ", " + endY + "), 持续时间: " + searchProductSwipeDuration + "ms");
                                            AutomationAccessibilityService.Swipe(startX, startY, endX, endY, searchProductSwipeDuration);
                                            AutomationAccessibilityService.Sleep(2500);
                                            break;
                                        }
                                        else {
                                            Log.d(TAG, "节点文本和publisher匹配失败");
                                            continue;
                                        }
                                    }
                                    // break;
                                }

                            }

                        }

                    }
                    
                    // 说明遍历所有的节点，都没有找到包含发布人的TextView节点，继续滚动屏幕
                    Log.d(TAG, "未找到包含发布人的TextView节点,继续滚动屏幕");
                    // 获取搜索商品时滑动参数
                    int searchProductSwipeStartX = settingsManager.getSearchProductSwipeStartX();
                    int searchProductSwipeStartY = settingsManager.getSearchProductSwipeStartY();
                    int searchProductSwipeEndX = settingsManager.getSearchProductSwipeEndX();
                    int searchProductSwipeEndYOffset = settingsManager.getSearchProductSwipeEndYOffset();
                    int searchProductSwipeDuration = settingsManager.getSearchProductSwipeDuration();
                    
                    // 计算起始坐标（0表示屏幕中心，负数表示从底部偏移）
                    int startX = (searchProductSwipeStartX == 0) ? (screenWidth / 2) : searchProductSwipeStartX;
                    int startY = (searchProductSwipeStartY <= 0) ? (screenHeight + searchProductSwipeStartY) : searchProductSwipeStartY;
                    
                    // 计算结束坐标（固定向上滚动900像素）
                    int endX = (searchProductSwipeEndX == 0) ? (screenWidth / 2) : searchProductSwipeEndX;
                    int endY = screenHeight + searchProductSwipeEndYOffset - 900; // 固定滚动900像素
                    
                    // 滚动屏幕
                    Log.d(TAG, "搜索商品时继续滚动: (" + startX + ", " + startY + ") -> (" + endX + ", " + endY + "), 持续时间: " + searchProductSwipeDuration + "ms");
                    AutomationAccessibilityService.Swipe(startX, startY, endX, endY, searchProductSwipeDuration);
                    AutomationAccessibilityService.Sleep(2000);
                } 
                else {
                    Log.w(TAG, "未找到id为com.shizhuang.duapp:id/tvUsername的节点,继续滚动屏幕");
                    // 获取搜索商品时滑动参数
                    int searchProductSwipeStartX = settingsManager.getSearchProductSwipeStartX();
                    int searchProductSwipeStartY = settingsManager.getSearchProductSwipeStartY();
                    int searchProductSwipeEndX = settingsManager.getSearchProductSwipeEndX();
                    int searchProductSwipeEndYOffset = settingsManager.getSearchProductSwipeEndYOffset();
                    int searchProductSwipeDuration = settingsManager.getSearchProductSwipeDuration();
                    
                    // 计算起始坐标（0表示屏幕中心，负数表示从底部偏移）
                    int startX = (searchProductSwipeStartX == 0) ? (screenWidth / 2) : searchProductSwipeStartX;
                    int startY = (searchProductSwipeStartY <= 0) ? (screenHeight + searchProductSwipeStartY) : searchProductSwipeStartY;
                    
                    // 计算结束坐标（固定向上滚动900像素）
                    int endX = (searchProductSwipeEndX == 0) ? (screenWidth / 2) : searchProductSwipeEndX;
                    int endY = screenHeight + searchProductSwipeEndYOffset - 900; // 固定滚动900像素
                    
                    // 滚动屏幕
                    Log.d(TAG, "搜索商品时继续滚动: (" + startX + ", " + startY + ") -> (" + endX + ", " + endY + "), 持续时间: " + searchProductSwipeDuration + "ms");
                    AutomationAccessibilityService.Swipe(startX, startY, endX, endY, searchProductSwipeDuration);
                    AutomationAccessibilityService.Sleep(2000);
                }
            }
            
            
        } catch (Exception e) {
            Log.e(TAG, "滚动屏幕失败", e);
            return false;
        }
    }
    
    /**
     * 计算节点的层级深度（从根节点到当前节点的路径长度）
     * @param node 目标节点
     * @return 节点的层级深度，如果节点为null则返回0
     */
    private int getNodeDepth(AccessibilityNodeInfo node) {
        if (node == null) {
            return 0;
        }
        
        int depth = 0;
        AccessibilityNodeInfo parent = node.getParent();
        while (parent != null) {
            depth++;
            AccessibilityNodeInfo temp = parent.getParent();
            // 回收父节点资源（getParent()返回的节点需要手动回收）
            parent.recycle();
            parent = temp;
        }
        
        return depth;
    }
}
