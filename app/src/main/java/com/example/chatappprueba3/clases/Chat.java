package com.example.chatappprueba3.clases;

import com.example.chatappprueba3.enums.MessageType;
import com.example.chatappprueba3.utils.Encrypter;

public class Chat {

    private String id;
    private String userSendId;
    private String userReceiveId;
    private String message;
    private boolean messageRead;
    private String date;
    private String time;
    private MessageType messageType;


    public Chat(){

    }

    public Chat(String id, String userSendId, String userReceiveId, String message, boolean messageRead, String date, String time, MessageType messageType){
        this.id = id;
        this.userSendId = userSendId;
        this.userReceiveId = userReceiveId;
        this.message = message;
        this.messageRead = messageRead;
        this.date = date;
        this.time = time;
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserSendId() {
        return userSendId;
    }

    public void setUserSendId(String userSendId) {
        this.userSendId = userSendId;
    }

    public String getUserReceiveId() {
        return userReceiveId;
    }

    public void setUserReceiveId(String userReceiveId) {
        this.userReceiveId = userReceiveId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getMessageRead() {
        return messageRead;
    }

    public void setMessageRead(boolean messageRead) {
        this.messageRead = messageRead;
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

}
