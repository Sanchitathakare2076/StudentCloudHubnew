package com.studentcloudhub;

public class PdfModel {

    private String id;
    private String name;
    private String url;
    private long createdAt;

    public PdfModel() {
    }

    public PdfModel(
            String id,
            String name,
            String url,
            long createdAt) {

        this.id = id;
        this.name = name;
        this.url = url;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}