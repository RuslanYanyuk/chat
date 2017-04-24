package com.chat.models;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
public class Message {
    private String data;
    private Sender sender;
    private ChatRoom chatRoom;

    public Message(String data, Sender sender, ChatRoom chatRoom) {
        this.data = data;
        this.sender = sender;
        this.chatRoom = chatRoom;
    }

    public String getData() {
        return data;
    }

    public Sender getSender() {
        return sender;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }
}
