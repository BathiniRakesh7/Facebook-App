package com.example.facebook;

public class Messages {
    public String date,time,message,type,messageFrom;

    public  Messages(){

    }

    public Messages(String date, String time, String message, String type, String messageFrom) {
        this.date = date;
        this.time = time;
        this.message = message;
        this.type = type;
        this.messageFrom = messageFrom;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageFrom() {
        return messageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        this.messageFrom = messageFrom;
    }
}
