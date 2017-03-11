package com.personal.firebase;

public class User {
    public String uid;
    public String email;
    public long firebaseToken;

    public User() {}

    public User(String uid, String email, long firebaseToken) {
        this.uid = uid;
        this.email = email;
        this.firebaseToken = firebaseToken;
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

    public long getFirebaseToken() {
        return firebaseToken;
    }

    public void setFirebaseToken(long firebaseToken) {
        this.firebaseToken = firebaseToken;
    }
}
