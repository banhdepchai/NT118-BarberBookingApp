package com.example.androidbarberapp.Model;

import com.google.firebase.firestore.FieldValue;

public class MyNotification {
    private String uid, title, content;
    private boolean read;
    private FieldValue serverTimestamp;


    public MyNotification() {
    }

//    public MyNotification(String uid, String title, String content, boolean read) {
//        this.uid = uid;
//        this.title = title;
//        this.content = content;
//        this.read = read;
//    }

    public String getUid() {
        return uid;
    }

    public MyNotification setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MyNotification setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContent() {
        return content;
    }

    public MyNotification setContent(String content) {
        this.content = content;
        return this;
    }

    public boolean isRead() {
        return read;
    }

    public MyNotification setRead(boolean read) {
        this.read = read;
        return this;
    }

    public FieldValue getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(FieldValue serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }
}
