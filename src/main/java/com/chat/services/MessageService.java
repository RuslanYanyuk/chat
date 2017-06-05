package com.chat.services;

import com.chat.exceptions.AccessDeniedException;
import com.chat.models.ChatRoom;
import com.chat.models.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
@Service
public class MessageService {

    public static final String NOT_PARTICIPANT = "User is not participant of the chat room";

    @Autowired
    KafkaTemplate<Long, String> kafkaTemplate;

    @Autowired
    UserService userService;

    public void sendMessage(Message msg) throws AccessDeniedException {
        List<ChatRoom> chatRooms = userService.getChatRooms(msg.getSender());
        ChatRoom chatRoom = new ChatRoom(msg.getChatRoom().getTopic());

        if (!chatRooms.contains(chatRoom)) {
            throw new AccessDeniedException(NOT_PARTICIPANT);
        }
        kafkaTemplate.send(chatRoom.getTopic(), msg.getSender().getId(), msg.getData());
    }
}
