package com.personal.firebase;

public class User {
    public String uid;
    public String email;
    public long customId;

    public User() {}

    public User(String uid, String email, long customId) {
        this.uid = uid;
        this.email = email;
        this.customId = customId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCustomId() {
        return customId;
    }

    public void setCustomId(long firebaseToken) {
        this.customId = firebaseToken;
    }
}
