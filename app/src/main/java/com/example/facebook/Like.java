package com.example.facebook;

public class Like {
    private String userId;

    public Like() {
        // Default constructor required for Firebase
    }

    public Like(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
