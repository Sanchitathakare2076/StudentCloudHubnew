package com.studentcloudhub;

public class Model {

    public String id;
    public String title;
    public String subtitle;
    public String description;
    public String status;
    public String date;
    public String userId;
    public boolean shared;

    // Required empty constructor for Firebase
    public Model() {
    }

    public Model(String id,
                 String title,
                 String subtitle,
                 String description,
                 String statusOrDate) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.status = statusOrDate;
        this.date = statusOrDate;
    }

    public Model(String id,
                 String title,
                 String subtitle,
                 String description,
                 String status,
                 String date,
                 String userId,
                 boolean shared) {

        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.status = status;
        this.date = date;
        this.userId = userId;
        this.shared = shared;
    }
}