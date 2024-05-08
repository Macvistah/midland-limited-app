package com.example.agro_irrigation.Models;

public class Message {
    public Message() {
    }

    public Message(int id, ChatUsers sender, String message, String createdAt) {
        this.id = id;
        this.sender = sender;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ChatUsers getSender() {
        return sender;
    }

    public void setSender(ChatUsers sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    int id;
    ChatUsers sender;
    String message;
    String createdAt;
}
