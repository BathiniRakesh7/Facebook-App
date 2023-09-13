package com.example.facebook;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Upload {
    private String mName;
    private String mImageUrl;
    private String mKey;

    private int mLikes;

    private List<String> mComments;

    public Upload() {
        //empty constructor needed
    }

    public Upload(String name, String imageUrl) {
        if (name.trim().equals("")) {
            name = "No Name";
        }

        mName = name;
        mImageUrl = imageUrl;
        mComments = new ArrayList<>();

    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
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

    public List<String> getComments() {
        return mComments;
    }

    public void setComments(List<String> comments) {
        mComments = comments;
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
