package com.zh.dewuautomationscript;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class UrlManagerActivity extends AppCompatActivity implements UrlAdapter.OnUrlActionListener {
    
    private TextInputEditText etUrlInput;
    private Button btnAddUrl;
    private Button btnImportFromFile;
    private Button btnSaveToFile;
    private RecyclerView rvUrlList;
    private TextView tvUrlCount;
    private TextView tvEmptyState;
    private TextView tvStatus;
    
    private UrlAdapter urlAdapter;
    private List<UrlItem> urlList;
    private JsonFileManager jsonFileManager;
    
    // 文件选择器
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_manager);
        
        initViews();
        initData();
        setupRecyclerView();
        setClickListeners();
        loadUrlsFromFile();
    }

    private void initViews() {
        etUrlInput = findViewById(R.id.etUrlInput);
        btnAddUrl = findViewById(R.id.btnAddUrl);
        btnImportFromFile = findViewById(R.id.btnImportFromFile);
        btnSaveToFile = findViewById(R.id.btnSaveToFile);
        rvUrlList = findViewById(R.id.rvUrlList);
        tvUrlCount = findViewById(R.id.tvUrlCount);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void initData() {
        urlList = new ArrayList<>();
        jsonFileManager = new JsonFileManager(this);
        
        // 初始化文件选择器
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        importUrlsFromFile(uri);
                    }
                }
            }
        );
    }

    private void setupRecyclerView() {
        urlAdapter = new UrlAdapter(urlList, this);
        rvUrlList.setLayoutManager(new LinearLayoutManager(this));
        rvUrlList.setAdapter(urlAdapter);
    }

    private void setClickListeners() {
        btnAddUrl.setOnClickListener(v -> addUrl());
        btnImportFromFile.setOnClickListener(v -> openFilePicker());
        btnSaveToFile.setOnClickListener(v -> saveUrlsToFile());
    }

    private void addUrl() {
        String urlText = etUrlInput.getText().toString().trim();
        
        if (TextUtils.isEmpty(urlText)) {
            Toast.makeText(this, "请输入链接地址", Toast.LENGTH_SHORT).show();
            return;
        }

        // 简单的URL验证
        if (!isValidUrl(urlText)) {
            Toast.makeText(this, "请输入有效的链接地址", Toast.LENGTH_SHORT).show();
            return;
        }

        // 确保URL有协议前缀（支持得物深度链接）
        if (!urlText.startsWith("http://") && !urlText.startsWith("https://") && !urlText.startsWith("dewulink://")) {
            urlText = "https://" + urlText;
        }

        // 创建URL项目
        UrlItem urlItem = new UrlItem();
        urlItem.setUrl(urlText);
        urlItem.setTitle(extractTitleFromUrl(urlText));

        // 检查是否已存在
        if (isUrlExists(urlText)) {
            Toast.makeText(this, "该链接已存在", Toast.LENGTH_SHORT).show();
            return;
        }

        urlList.add(urlItem);
        urlAdapter.notifyItemInserted(urlList.size() - 1);
        etUrlInput.setText("");
        updateUrlCount();
        updateEmptyState();
        
        showStatus("链接已添加");
    }

    private boolean isValidUrl(String url) {
        // 支持得物深度链接
        if (url.startsWith("dewulink://")) {
            return true;
        }
        // 支持普通HTTP/HTTPS链接
        return url.contains(".") && (url.contains("http") || url.startsWith("www.") || 
                url.matches("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"));
    }

    private String extractTitleFromUrl(String url) {
        try {
            // 处理得物深度链接
            if (url.startsWith("dewulink://")) {
                return "得物深度链接";
            }
            
            // 处理普通HTTP/HTTPS链接
            if (url.startsWith("http://")) {
                url = url.substring(7);
            } else if (url.startsWith("https://")) {
                url = url.substring(8);
            }
            if (url.contains("/")) {
                url = url.substring(0, url.indexOf("/"));
            }
            return url;
        } catch (Exception e) {
            return url;
        }
    }

    private boolean isUrlExists(String url) {
        for (UrlItem item : urlList) {
            if (item.getUrl().equals(url)) {
                return true;
            }
        }
        return false;
    }

    private void saveUrlsToFile() {
        if (urlList.isEmpty()) {
            Toast.makeText(this, "没有链接需要保存", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = jsonFileManager.saveUrlsToFile(urlList);
        if (success) {
            showStatus("已保存 " + urlList.size() + " 个链接到文件");
            Toast.makeText(this, "链接保存成功", Toast.LENGTH_SHORT).show();
        } else {
            showStatus("保存失败");
            Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUrlsFromFile() {
        List<UrlItem> loadedUrls = jsonFileManager.loadUrlsFromFile();
        urlList.clear();
        urlList.addAll(loadedUrls);
        urlAdapter.notifyDataSetChanged();
        updateUrlCount();
        updateEmptyState();
        
        if (!loadedUrls.isEmpty()) {
            showStatus("已加载 " + loadedUrls.size() + " 个链接");
        } else {
            showStatus("文件中没有保存的链接");
        }
    }

    @Override
    public void onDeleteUrl(int position) {
        if (position >= 0 && position < urlList.size()) {
            UrlItem removedItem = urlList.remove(position);
            urlAdapter.notifyItemRemoved(position);
            updateUrlCount();
            updateEmptyState();
            showStatus("已删除: " + removedItem.getTitle());
        }
    }

    @Override
    public void onEditUrl(int position, UrlItem urlItem) {
        showEditUrlDialog(position, urlItem);
    }

    private void showEditUrlDialog(int position, UrlItem urlItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_url, null);
        builder.setView(dialogView);

        TextInputEditText etEditTitle = dialogView.findViewById(R.id.etEditTitle);
        TextInputEditText etEditUrl = dialogView.findViewById(R.id.etEditUrl);
        Button btnCancelEdit = dialogView.findViewById(R.id.btnCancelEdit);
        Button btnSaveEdit = dialogView.findViewById(R.id.btnSaveEdit);

        // 设置当前值
        etEditTitle.setText(urlItem.getTitle());
        etEditUrl.setText(urlItem.getUrl());

        AlertDialog dialog = builder.create();

        btnCancelEdit.setOnClickListener(v -> dialog.dismiss());

        btnSaveEdit.setOnClickListener(v -> {
            String newTitle = etEditTitle.getText().toString().trim();
            String newUrl = etEditUrl.getText().toString().trim();

            if (TextUtils.isEmpty(newUrl)) {
                Toast.makeText(this, "请输入链接地址", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidUrl(newUrl)) {
                Toast.makeText(this, "请输入有效的链接地址", Toast.LENGTH_SHORT).show();
                return;
            }

            // 确保URL有协议前缀（支持得物深度链接）
            if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://") && !newUrl.startsWith("dewulink://")) {
                newUrl = "https://" + newUrl;
            }

            // 检查是否与其他链接重复
            boolean isDuplicate = false;
            for (int i = 0; i < urlList.size(); i++) {
                if (i != position && urlList.get(i).getUrl().equals(newUrl)) {
                    isDuplicate = true;
                    break;
                }
            }

            if (isDuplicate) {
                Toast.makeText(this, "该链接已存在", Toast.LENGTH_SHORT).show();
                return;
            }

            // 更新链接信息
            urlItem.setTitle(newTitle.isEmpty() ? null : newTitle);
            urlItem.setUrl(newUrl);
            urlAdapter.notifyItemChanged(position);
            dialog.dismiss();
            showStatus("链接已更新");
        });

        dialog.show();
    }

    private void updateUrlCount() {
        tvUrlCount.setText("(" + urlList.size() + ")");
    }

    private void updateEmptyState() {
        if (urlList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvUrlList.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvUrlList.setVisibility(View.VISIBLE);
        }
    }

    private void showStatus(String message) {
        tvStatus.setText(message);
        tvStatus.postDelayed(() -> tvStatus.setText(""), 3000);
    }
    
    /**
     * 打开文件选择器
     */
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(intent);
    }
    
    /**
     * 从选择的文件导入URL
     */
    private void importUrlsFromFile(Uri uri) {
        try {
            // 读取文件内容
            String jsonContent = readFileContent(uri);
            if (jsonContent == null || jsonContent.trim().isEmpty()) {
                Toast.makeText(this, "文件内容为空", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 解析JSON
            List<UrlItem> importedUrls = parseJsonToUrlList(jsonContent);
            if (importedUrls == null || importedUrls.isEmpty()) {
                Toast.makeText(this, "文件中没有有效的URL数据", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 显示导入确认对话框
            showImportConfirmDialog(importedUrls);
            
        } catch (Exception e) {
            Toast.makeText(this, "导入文件失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * 读取文件内容
     */
    private String readFileContent(Uri uri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            reader.close();
            inputStream.close();
            
            return content.toString();
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 解析JSON字符串为URL列表
     */
    private List<UrlItem> parseJsonToUrlList(String jsonContent) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<UrlItem>>(){}.getType();
            return gson.fromJson(jsonContent, listType);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 显示导入确认对话框
     */
    private void showImportConfirmDialog(List<UrlItem> importedUrls) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认导入");
        builder.setMessage("将导入 " + importedUrls.size() + " 个链接\n\n是否要替换当前所有链接？");
        
        builder.setPositiveButton("替换", (dialog, which) -> {
            // 替换所有链接
            urlList.clear();
            urlList.addAll(importedUrls);
            urlAdapter.notifyDataSetChanged();
            updateUrlCount();
            updateEmptyState();
            showStatus("已导入 " + importedUrls.size() + " 个链接");
            Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("追加", (dialog, which) -> {
            // 追加链接（去重）
            int addedCount = 0;
            for (UrlItem importedUrl : importedUrls) {
                if (!isUrlExists(importedUrl.getUrl())) {
                    urlList.add(importedUrl);
                    addedCount++;
                }
            }
            urlAdapter.notifyDataSetChanged();
            updateUrlCount();
            updateEmptyState();
            showStatus("已追加 " + addedCount + " 个新链接");
            Toast.makeText(this, "追加了 " + addedCount + " 个新链接", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNeutralButton("取消", null);
        builder.show();
    }
}
