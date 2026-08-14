package com.indigold.notification_system.dto;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String pushToken;
    private PreferencesResponse preferences;

    public UserResponse(
            Long id,
            String name,
            String email,
            String phone,
            String pushToken,
            PreferencesResponse preferences
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.pushToken = pushToken;
        this.preferences = preferences;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public PreferencesResponse getPreferences() {
        return preferences;
    }

    public void setPreferences(PreferencesResponse preferences) {
        this.preferences = preferences;
    }

    public static class PreferencesResponse {

        private boolean email;
        private boolean sms;
        private boolean push;
        private boolean inApp;

        public PreferencesResponse(
                boolean email,
                boolean sms,
                boolean push,
                boolean inApp
        ) {
            this.email = email;
            this.sms = sms;
            this.push = push;
            this.inApp = inApp;
        }

        public boolean isEmail() {
            return email;
        }

        public void setEmail(boolean email) {
            this.email = email;
        }

        public boolean isSms() {
            return sms;
        }

        public void setSms(boolean sms) {
            this.sms = sms;
        }

        public boolean isPush() {
            return push;
        }

        public void setPush(boolean push) {
            this.push = push;
        }

        public boolean isInApp() {
            return inApp;
        }

        public void setInApp(boolean inApp) {
            this.inApp = inApp;
        }
    }
}
