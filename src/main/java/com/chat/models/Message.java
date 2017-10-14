package com.chat.models;

import com.chat.utils.MessageSerializer;
import com.chat.utils.ZonedDateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
@JsonSerialize(using = MessageSerializer.class)
public class Message {
    private String data;
    private User sender;

    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private final ZonedDateTime timestamp = ZonedDateTime.now(ZoneOffset.UTC);
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

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }
}
