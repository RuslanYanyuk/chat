package com.chat.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
@Entity
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String topic;

    @ManyToMany
    @JoinTable(name = "chat_rooms_to_users",
            joinColumns = @JoinColumn(name = "chat_room_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private List<User> participants = new ArrayList<>();

    public ChatRoom() {
    }

    public ChatRoom(String topic) {
        this.topic = topic;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public void addParticipant(User user) {
        participants.add(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatRoom chatRoom = (ChatRoom) o;

        return topic.equals(chatRoom.topic);
    }

    @Override
    public int hashCode() {
        return topic.hashCode();
    }

    @Override
    public String toString() {
        return "ChatRoom{" +
                "id=" + id +
                ", topic='" + topic + '\'' +
                ", participants=" + participants +
                '}';
    }
}
