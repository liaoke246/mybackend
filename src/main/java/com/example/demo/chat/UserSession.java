package com.example.demo.chat;

public class UserSession {

    private final String userId;
    private String name;

    public UserSession(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasJoined() {
        return name != null && !name.isBlank();
    }

}
