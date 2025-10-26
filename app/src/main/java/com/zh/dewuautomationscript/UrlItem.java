package com.zh.dewuautomationscript;

import java.io.Serializable;

public class UrlItem implements Serializable {
    private String title;
    private String url;
    private long timestamp;

    public UrlItem() {
        this.timestamp = System.currentTimeMillis();
    }

    public UrlItem(String title, String url) {
        this.title = title;
        this.url = url;
        this.timestamp = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
                ", url='" + url + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
