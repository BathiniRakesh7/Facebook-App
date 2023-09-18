package com.example.facebook;

import java.util.List;

public class Comments {
    private String commentText;
    private String userId;
    private long timestamp;
    private List<Like> likes;
    private List<Reply> replies;
    private String userName;


    public Comments() {
        // Default constructor required for Firebase
    }

    public Comments(String commentText, String userId, long timestamp) {
        this.commentText = commentText;
        this.userId = userId;
        this.timestamp = timestamp;
//        this.likes = new ArrayList<>();
//        this.replies = new ArrayList<>();
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public List<Reply> getReplies() {
        return replies;
    }

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
