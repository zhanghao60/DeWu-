package com.zh.dewuautomationscript;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JsonFileManager {
    private static final String TAG = "JsonFileManager";
    private static final String FILE_NAME = "urls.json";
    
    private Context context;
    private Gson gson;

    public JsonFileManager(Context context) {
        this.context = context;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * 保存URL列表到JSON文件
     */
    public boolean saveUrlsToFile(List<UrlItem> urlList) {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            String json = gson.toJson(urlList);
            
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
            writer.write(json);
            writer.close();
            fos.close();
            
            Log.d(TAG, "URLs saved to file successfully");
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving URLs to file", e);
            return false;
        }
    }

    /**
     * 从JSON文件加载URL列表
     */
    public List<UrlItem> loadUrlsFromFile() {
        List<UrlItem> urlList = new ArrayList<>();
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (!file.exists()) {
                Log.d(TAG, "File does not exist, returning empty list");
                return urlList;
            }

            FileInputStream fis = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                json.append(line);
            }
            
            bufferedReader.close();
            reader.close();
            fis.close();

            Type listType = new TypeToken<List<UrlItem>>(){}.getType();
            urlList = gson.fromJson(json.toString(), listType);
            
            if (urlList == null) {
                urlList = new ArrayList<>();
            }
            
            Log.d(TAG, "URLs loaded from file successfully, count: " + urlList.size());
        } catch (IOException e) {
            Log.e(TAG, "Error loading URLs from file", e);
        }
        return urlList;
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists() {
        File file = new File(context.getFilesDir(), FILE_NAME);
        return file.exists();
    }

    /**
     * 删除JSON文件
     */
    public boolean deleteFile() {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            boolean deleted = file.delete();
            Log.d(TAG, "File deleted: " + deleted);
            return deleted;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting file", e);
            return false;
        }
    }

    /**
     * 获取文件大小
     */
    public long getFileSize() {
        File file = new File(context.getFilesDir(), FILE_NAME);
        return file.length();
    }
}
