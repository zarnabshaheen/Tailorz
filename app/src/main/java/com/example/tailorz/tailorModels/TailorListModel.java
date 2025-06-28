package com.example.tailorz.tailorModels;

public class TailorListModel {
    private String username;
    private String email;
    private String profileImageUrl; // New field for profile image URL

    public TailorListModel() {
        // Empty Constructor for Firebase
    }

    public TailorListModel(String username, String email, String profileImageUrl) {
        this.username = username;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
