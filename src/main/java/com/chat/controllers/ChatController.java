package com.chat.controllers;

import com.chat.exceptions.AccessDeniedException;
import com.chat.exceptions.MessageDeliveryException;
import com.chat.models.ChatRoom;
import com.chat.models.Message;
import com.chat.models.User;
import com.chat.services.MessageService;
import com.chat.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
@Controller
public class ChatController {

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @MessageMapping("/message/{room}")
    public void sendMessage(Message message, @DestinationVariable String room, Principal principal) throws AccessDeniedException, MessageDeliveryException {
        message.setChatRoom(new ChatRoom(room));
        message.setSender(new User(principal.getName(), null));
        messageService.sendMessage(message);
    }

    @MessageMapping("/get-chat-rooms")
    @SendToUser("/topic/chat-rooms")
    public List<ChatRoom> getChatRooms(Principal principal) {
        return userService.getChatRooms(new User(principal.getName(), null));
    }

    @MessageMapping("/create-chat-room")
    @SendToUser("/topic/chat-rooms")
    public List<ChatRoom> createChatRooms(User[] participants, Principal principal) throws AccessDeniedException {
        User user = new User(principal.getName(), null);

        userService.addChatRoom(user, participants);
        return userService.getChatRooms(user);
    }

    @MessageMapping("/get-contacts")
    @SendToUser("/topic/contacts")
    public List<User> getContacts(Principal principal) throws AccessDeniedException {
        return userService.getContacts(new User(principal.getName(), null));
    }

    @MessageMapping("/find-users")
    @SendToUser("/topic/found-users")
    public List<User> findUsers(User user) throws AccessDeniedException {
        if (user.getName() == null || user.getName().isEmpty()) {
            return new ArrayList<>();
        }
        return userService.findUserByName(user.getUsername());
    }

    @MessageMapping("/add-contact")
    @SendToUser("/topic/contacts")
    public List<User> addContact(User user, Principal principal) throws AccessDeniedException {
        return userService.addContact(new User(principal.getName(), null), user);
    }

    /**
     * Send to user at "/user/topic/chat-history"
     */
    @MessageMapping("/get-chat-history/{topic}")
    public void getChatHistory(@DestinationVariable String topic, Principal principal) {
        messageService.getAllMessages(topic, principal.getName());
    }
}
