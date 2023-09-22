package com.example.facebook;
public class Comments {
    public String comment,dateTime,userName;

    public Comments() {
        // Default constructor required for Firebase
    }

    public Comments(String comment, String dateTime, String userName) {
        this.comment = comment;
        this.dateTime = dateTime;
        this.userName = userName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
