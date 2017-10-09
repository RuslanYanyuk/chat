package com.chat.services;

import com.chat.exceptions.AccessDeniedException;
import com.chat.models.ChatRoom;
import com.chat.models.User;
import com.chat.repositories.ChatRoomRepository;
import com.chat.repositories.UserRepository;
import com.chat.utils.KafkaAdminUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

/**
 * @author Ruslan Yaniuk
 * @date May 2017
 */
@Service
public class UserService implements UserDetailsService {

    public static final String USER_IS_NOT_CONTACT = "User is not from the contacts list";

    @Autowired
    UserRepository userRepository;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Autowired
    KafkaAdminUtils kafkaAdminUtils;

    public User add(User user) {
        return userRepository.save(user);
    }

    /**
     * Avoid circular references
     */
    @Transactional
    public List<ChatRoom> getChatRooms(User user) {
        user = findUser(user.getName());
        Hibernate.initialize(user.getChatRooms());
        List<ChatRoom> chatRooms = user.getChatRooms();
        chatRooms.forEach(chatRoom -> Hibernate.initialize(chatRoom.getParticipants()));
        chatRooms.forEach(chatRoom ->
                        chatRoom.getParticipants().forEach(u -> u.setChatRooms(null))
        );
        return chatRooms;
    }

    @Transactional
    public ChatRoom addChatRoom(User owner, User... participants) throws AccessDeniedException {
        ChatRoom chatRoom = new ChatRoom(UUID.randomUUID().toString());

        owner = findUser(owner.getName());
        Hibernate.initialize(owner.getContacts());
        chatRoom.addParticipant(owner);
        for (User u : participants) {
            u = findUser(u.getName());
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
        user = findUser(user.getName());
        Hibernate.initialize(user.getContacts());
        return user.getContacts();
    }

    @Transactional
    public List<User> addContact(User owner, User contact) throws AccessDeniedException {
        if (owner.getName().equals(contact.getName())) {
            return getContacts(owner);
        }
        owner = findUser(owner.getName());
        contact = findUser(contact.getName());
        Hibernate.initialize(owner.getContacts());
        if (owner.getContacts().contains(contact)) {
            return owner.getContacts();
        }
        Hibernate.initialize(contact.getContacts());
        owner.getContacts().add(contact);
        contact.getContacts().add(owner);
        userRepository.save(owner);
        userRepository.save(contact);
        addChatRoom(owner, contact);
        return owner.getContacts();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new User(findUser(username));
    }

    public List<User> findUserByName(String usernamePrefix) {
        return userRepository.findByNameStartingWithOrderByNameAsc(usernamePrefix);
    }

    private User findUser(String username) {
        List<User> users = userRepository.findByName(username);
        if (users.isEmpty() || users.size() > 1) {
            throw new UsernameNotFoundException("Can't find the user of the specified name");
        }
        return users.get(0);
    }
}