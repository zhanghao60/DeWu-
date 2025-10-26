package com.zh.dewuautomationscript;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UrlAdapter extends RecyclerView.Adapter<UrlAdapter.UrlViewHolder> {
    
    private List<UrlItem> urlList;
    private OnUrlActionListener listener;

    public interface OnUrlActionListener {
        void onDeleteUrl(int position);
        void onEditUrl(int position, UrlItem urlItem);
    }

    public UrlAdapter(List<UrlItem> urlList, OnUrlActionListener listener) {
        this.urlList = urlList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UrlViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_url, parent, false);
        return new UrlViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UrlViewHolder holder, int position) {
        UrlItem urlItem = urlList.get(position);
        holder.bind(urlItem, position);
    }

    @Override
    public int getItemCount() {
        return urlList.size();
    }

    public class UrlViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUrlTitle;
        private TextView tvUrlAddress;
        private ImageButton btnEditUrl;
        private ImageButton btnOpenUrl;
        private ImageButton btnDeleteUrl;

        public UrlViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUrlTitle = itemView.findViewById(R.id.tvUrlTitle);
            tvUrlAddress = itemView.findViewById(R.id.tvUrlAddress);
            btnEditUrl = itemView.findViewById(R.id.btnEditUrl);
            btnOpenUrl = itemView.findViewById(R.id.btnOpenUrl);
            btnDeleteUrl = itemView.findViewById(R.id.btnDeleteUrl);
        }

        public void bind(UrlItem urlItem, int position) {
            // 设置标题，如果没有标题则使用URL
            String title = urlItem.getTitle();
            if (title == null || title.trim().isEmpty()) {
                title = extractDomainFromUrl(urlItem.getUrl());
            }
            tvUrlTitle.setText(title);
            tvUrlAddress.setText(urlItem.getUrl());

            // 编辑链接按钮
            btnEditUrl.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditUrl(position, urlItem);
                }
            });

            // 打开链接按钮
            btnOpenUrl.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(urlItem.getUrl()));
                    itemView.getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(itemView.getContext(), "无法打开链接", Toast.LENGTH_SHORT).show();
                }
            });

            // 删除链接按钮
            btnDeleteUrl.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteUrl(position);
                }
            });
        }

        private String extractDomainFromUrl(String url) {
            try {
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
    }
}
