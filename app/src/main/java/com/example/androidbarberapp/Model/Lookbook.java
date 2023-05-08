package com.example.androidbarberapp.Model;

import android.net.Uri;

public class Lookbook {
    private String image;

    public Lookbook() {
    }

    public Lookbook(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }
}
