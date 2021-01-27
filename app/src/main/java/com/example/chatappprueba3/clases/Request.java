package com.example.chatappprueba3.clases;

public class Request {

    String status;
    String idChat;

    public Request(){

    }

    public Request(String status){
        this.status = status;
        this.idChat = "";
    }

    public Request(String status, String idChat){
        this.status = status;
        this.idChat = idChat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIdChat() {
        return idChat;
    }

    public void setIdChat(String idChat) {
        this.idChat = idChat;
    }
}
