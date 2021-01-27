package com.example.chatappprueba3.clases;

public class Status {

    private String status;
    private String date;
    private String time;
    private String chat;

    public Status(){

    }

    public Status(String status, String date, String time, String chat){
        this.status = status;
        this.date = date;
        this.time = time;
        this.chat = chat;
    }

    public Status(String status){
        this.status = status;
        this.date = "";
        this.time= "";
        this.chat = "";
    }

    public Status(String status, String chat){
        this.status = status;
        this.date = "";
        this.time = "";
        this.chat = chat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }
}
