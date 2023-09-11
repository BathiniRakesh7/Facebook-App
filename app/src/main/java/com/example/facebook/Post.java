package com.example.facebook;

public class Post {
    private String postText;
    private String imageUrl;

    public Post(String postText, String imageUrl) {
        this.postText = postText;
        this.imageUrl = imageUrl;
    }

    public String getPostText() {
        return postText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}
