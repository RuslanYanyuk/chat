package com.chat.services;

import com.chat.exceptions.AccessDeniedException;
import com.chat.models.ChatRoom;
import com.chat.models.User;
import com.chat.repositories.ChatRoomRepository;
import com.chat.repositories.UserRepository;
import com.chat.utils.KafkaAdminUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * @author Ruslan Yaniuk
 * @date May 2017
 */
@Service
public class UserService {

    public static final String USER_IS_NOT_CONTACT = "User is not from the contacts list";

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    KafkaAdminUtils kafkaAdminUtils;

    @Transactional
    public List<ChatRoom> getChatRooms(User user) {
        user = userRepository.findByName(user.getName());
        Hibernate.initialize(user.getChatRooms());
        return user.getChatRooms();
    }

    @Transactional
    public ChatRoom addChatRoom(User owner, User[] participants) throws AccessDeniedException {
        ChatRoom chatRoom = new ChatRoom(UUID.randomUUID().toString());

        owner = userRepository.findByName(owner.getName());
        Hibernate.initialize(owner.getContacts());
        chatRoom.addParticipant(owner);
        for (User u : participants) {
            u = userRepository.findByName(u.getName());
            if (!owner.getContacts().contains(u)) {
                throw new AccessDeniedException(USER_IS_NOT_CONTACT);
            }
            chatRoom.addParticipant(u);
        }
        try {
            kafkaAdminUtils.crateTopic(chatRoom.getTopic());
            chatRoom = chatRoomRepository.save(chatRoom);
        } catch (RuntimeException e) {
            if (chatRoomRepository.findOne(chatRoom.getTopic()) != null) {
                chatRoomRepository.delete(chatRoom.getTopic());
            }
            if (!kafkaAdminUtils.topicExists(chatRoom.getTopic())) {
                kafkaAdminUtils.deleteTopic(chatRoom.getTopic());
            }
            throw new RuntimeException(e);
        }
        return chatRoom;
    }

    @Transactional
    public List<User> getContacts(User user) {
        user = userRepository.findByName(user.getName());
        Hibernate.initialize(user.getContacts());
        return user.getContacts();
    }
}
