package com.studentcloudhub;

public class Subject {

    private String id;
    private String name;
    private long createdAt;

    public Subject() {
    }

    public Subject(String id, String name, long createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}