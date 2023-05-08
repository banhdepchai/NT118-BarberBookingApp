package com.example.androidbarberapp.Model;

public class User {
    public String name, address, email;

    public User(String name, String address, String email) {
        this.name = name;
        this.address = address;
        this.email = email;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

}
