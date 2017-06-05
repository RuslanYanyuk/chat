package com.chat.models;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
public class Message {
    private String data;
    private User sender;
    private ChatRoom chatRoom;

    public Message() {
    }

    public Message(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
}
