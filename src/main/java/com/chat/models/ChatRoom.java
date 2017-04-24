package com.chat.models;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
public class ChatRoom {
    private String topic;

    public ChatRoom(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }
}
