package com.example.facebook;
public class Friends {
    private String dateTime;

    public Friends() {
        // Default constructor required for Firebase
    }

    public Friends(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}

