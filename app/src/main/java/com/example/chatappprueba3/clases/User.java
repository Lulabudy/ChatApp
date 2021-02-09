package com.example.chatappprueba3.clases;

public class User {

    private String id;
    private String name;
    private String email;
    private String avatar;
    private String date;
    private Boolean showOnlinePrivacy;
    private Boolean showReadMessage;

    public User(){

    }



    public User(String id, String name, String email, String avatar, String date, Boolean showOnlinePrivacy, Boolean showReadMessage) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.date = date;
        this.showOnlinePrivacy = showOnlinePrivacy;
        this.showReadMessage = showReadMessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Boolean isShowOnlinePrivacy() {
        return showOnlinePrivacy;
    }

    public void setShowOnlinePrivacy(Boolean showOnlinePrivacy) {
        this.showOnlinePrivacy = showOnlinePrivacy;
    }

    public Boolean getShowReadMessage() {
        return showReadMessage;
    }

    public void setShowReadMessage(Boolean showReadMessage) {
        this.showReadMessage = showReadMessage;
    }
}
