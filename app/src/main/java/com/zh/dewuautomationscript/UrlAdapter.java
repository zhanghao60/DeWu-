package com.zh.dewuautomationscript;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

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
        private ImageButton btnDeleteUrl;

        public UrlViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUrlTitle = itemView.findViewById(R.id.tvUrlTitle);
            tvUrlAddress = itemView.findViewById(R.id.tvUrlAddress);
            btnEditUrl = itemView.findViewById(R.id.btnEditUrl);
            btnDeleteUrl = itemView.findViewById(R.id.btnDeleteUrl);
        }

        public void bind(UrlItem urlItem, int position) {
            // 设置商品标题
            String title = urlItem.getTitle();
            if (title == null || title.trim().isEmpty()) {
                title = "未命名商品";
            }
            tvUrlTitle.setText(title);
            
            // 设置发布人
            String publisher = urlItem.getPublisher();
            if (publisher == null || publisher.trim().isEmpty()) {
                publisher = "未知发布人";
            }
            tvUrlAddress.setText("发布人: " + publisher);

            // 编辑商品按钮
            btnEditUrl.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditUrl(position, urlItem);
                }
            });

            // 删除商品按钮
            btnDeleteUrl.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteUrl(position);
                }
            });
        }
    }
}
