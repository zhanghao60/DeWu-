package com.zh.dewuautomationscript;

import android.util.Log;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 激活码验证API服务
 * 负责与后端服务器通信验证激活码
 */
public class ActivationApiService {
    
    private static final String TAG = "ActivationApiService";
    private static final String BASE_URL = "https://dewu.rong.world/api/dewu/";
    private static final String ACTIVATE_ENDPOINT = "activate_code";
    
    private OkHttpClient httpClient;
    
    public ActivationApiService() {
        // 创建HTTP客户端，设置超时时间
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 激活码验证回调接口
     */
    public interface ActivationCallback {
        void onSuccess(String message, String validTime);
        void onError(String error);
    }
    
    /**
     * 验证激活码
     * @param activateCode 激活码
     * @param callback 回调接口
     */
    public void verifyActivateCode(String activateCode, ActivationCallback callback) {
        if (activateCode == null || activateCode.trim().isEmpty()) {
            callback.onError("激活码不能为空");
            return;
        }
        
        try {
            // 构建form-data请求体
            RequestBody requestBody = new FormBody.Builder()
                    .add("activate_code", activateCode.trim())
                    .build();
            
            // 构建请求
            Request request = new Request.Builder()
                    .url(BASE_URL + ACTIVATE_ENDPOINT)
                    .post(requestBody)
                    .build();
            
            Log.d(TAG, "发送激活码验证请求: " + activateCode);
            
            // 异步执行请求
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "激活码验证请求失败", e);
                    callback.onError("网络请求失败: " + e.getMessage());
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        Log.d(TAG, "激活码验证响应: " + response.code() + " - " + responseBody);
                        
                        if (response.isSuccessful()) {
                            // 检查响应体中的实际状态
                            try {
                                JSONObject jsonResponse = new JSONObject(responseBody);
                                int code = jsonResponse.optInt("code", 200);
                                String message = jsonResponse.optString("message", "");
                                String validTime = jsonResponse.optString("time", "7days"); // 默认7天
                                
                                if (code == 200) {
                                    // 业务状态码200表示激活成功
                                    callback.onSuccess("激活码验证成功", validTime);
                                } else {
                                    // 业务状态码非200表示激活失败
                                    callback.onError("激活码验证失败: " + message);
                                }
                            } catch (JSONException e) {
                                // 如果响应不是JSON格式，按原来的逻辑处理
                                Log.w(TAG, "响应不是有效的JSON格式，按HTTP状态码处理");
                                callback.onSuccess("激活码验证成功", "7days");
                            }
                        } else {
                            // HTTP状态码非200表示请求失败
                            callback.onError("网络请求失败，状态码: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "处理激活码验证响应时出错", e);
                        callback.onError("响应处理失败: " + e.getMessage());
                    } finally {
                        if (response.body() != null) {
                            response.body().close();
                        }
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "构建激活码验证请求时出错", e);
            callback.onError("请求构建失败: " + e.getMessage());
        }
    }
    
    /**
     * 同步验证激活码（用于测试）
     * @param activateCode 激活码
     * @return 验证结果
     */
    public boolean verifyActivateCodeSync(String activateCode) {
        if (activateCode == null || activateCode.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 构建form-data请求体
            RequestBody requestBody = new FormBody.Builder()
                    .add("activate_code", activateCode.trim())
                    .build();
            
            // 构建请求
            Request request = new Request.Builder()
                    .url(BASE_URL + ACTIVATE_ENDPOINT)
                    .post(requestBody)
                    .build();
            
            Log.d(TAG, "发送同步激活码验证请求: " + activateCode);
            
            // 同步执行请求
            try (Response response = httpClient.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "同步激活码验证响应: " + response.code() + " - " + responseBody);
                
                if (response.isSuccessful()) {
                    // 检查响应体中的实际状态
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        int code = jsonResponse.optInt("code", 200);
                        return code == 200; // 只有业务状态码200才表示成功
                    } catch (JSONException e) {
                        // 如果响应不是JSON格式，按原来的逻辑处理
                        Log.w(TAG, "同步验证响应不是有效的JSON格式，按HTTP状态码处理");
                        return true;
                    }
                }
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "同步激活码验证失败", e);
            return false;
        }
    }
    
    /**
     * 释放资源
     */
    public void release() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
        }
    }
}
