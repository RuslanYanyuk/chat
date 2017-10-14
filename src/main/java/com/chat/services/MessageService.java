package com.chat.services;

import com.chat.exceptions.AccessDeniedException;
import com.chat.exceptions.MessageDeliveryException;
import com.chat.models.ChatRoom;
import com.chat.models.Message;
import com.chat.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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

    @Autowired
    ObjectMapper objectMapper;

    public void sendMessage(Message msg) throws AccessDeniedException, MessageDeliveryException {
        User sender = msg.getSender();
        ChatRoom chatRoom = new ChatRoom(msg.getChatRoom().getTopic());

        if (!userService.getChatRooms(sender).contains(chatRoom)) { //TODO implement cache
            throw new AccessDeniedException(NOT_PARTICIPANT);
        }
        kafkaTemplate.send(chatRoom.getTopic(), null, serialize(msg)); //TODO set the key
    }

    public String serialize(Message msg) throws MessageDeliveryException {
        try {
            return objectMapper.writeValueAsString(msg);
        } catch (JsonProcessingException e) {
            throw new MessageDeliveryException("Could not convert the message", e);
        }
    }
}
