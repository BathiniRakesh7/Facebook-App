package com.example.facebook;

import com.google.firebase.firestore.PropertyName;

public class FindFriends {
    @PropertyName("FullName")
    private String fullName;

    public FindFriends() {
        // Default constructor with no arguments
    }
    public FindFriends(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
