package com.example.facebook;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Upload {
    private String description;
    private String mImageUrl;
    private String userName; // Add this field

    private String mKey;

    private int mLikes;

    private List<Comments> mComments;

    private String mUserId;
    private String uploadDate;
    private String uploadTime;



    public Upload() {
        //empty constructor needed
    }

    public Upload(String description, String imageUrl, String userId , String uploadDate, String uploadTime,String userName) {
        this.userName = userName;
        this.description = description;
        mImageUrl = imageUrl;
        mComments = new ArrayList<>();
        mUserId = userId;
        this.uploadDate = uploadDate;
        this.uploadTime = uploadTime;

    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        description = description;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public int getLikes() {
        return mLikes;
    }

    public void setLikes(int likes) {
        mLikes = likes;
    }

    public List<Comments> getComments() {
        return mComments;
    }

    public void setComments(List<Comments> comments) {
        mComments = comments;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId) {
        mUserId = userId;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String key) {
        mKey = key;
    }
}
