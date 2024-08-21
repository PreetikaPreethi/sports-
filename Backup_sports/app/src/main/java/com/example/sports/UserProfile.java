package com.example.sports;
public class UserProfile {
    private String username;
    private String bio;
    private String profileImageUrl; // If you are including profile image URL

    // Default constructor required for calls to DataSnapshot.getValue(UserProfile.class)
    public UserProfile() {}

    public UserProfile(String username, String bio ) {
        this.username = username;
        this.bio = bio;
        this.profileImageUrl = null; // Set this to a default value if necessary
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }


    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
