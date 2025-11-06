package com.zh.dewuautomationscript;

import java.io.Serializable;

public class UrlItem implements Serializable {
    private String title;
    private String publisher;
    private long timestamp;

    public UrlItem() {
        this.timestamp = System.currentTimeMillis();
    }

    public UrlItem(String title, String publisher) {
        this.title = title;
        this.publisher = publisher;
        this.timestamp = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    // 为了向后兼容，保留getUrl和setUrl方法，但映射到publisher
    @Deprecated
    public String getUrl() {
        return publisher;
    }

    @Deprecated
    public void setUrl(String url) {
        this.publisher = url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UrlItem{" +
                "title='" + title + '\'' +
                ", publisher='" + publisher + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
