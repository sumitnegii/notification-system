package com.indigold.notification_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String phone;

    private String pushToken;

    public User() {
    }

    public User(String name, String email, String phone, String pushToken) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.pushToken = pushToken;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }
}