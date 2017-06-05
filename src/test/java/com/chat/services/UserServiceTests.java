package com.chat.services;

import com.chat.common.AbstractKafkaTest;
import com.chat.exceptions.AccessDeniedException;
import com.chat.models.ChatRoom;
import com.chat.models.User;
import com.chat.repositories.ChatRoomRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.test.MessageFixtures.getTestMessage;
import static com.test.UserFixtures.getArrayOfUsers;
import static com.test.UserFixtures.getUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasKey;

/**
 * @author Ruslan Yaniuk
 * @date May 2017
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class UserServiceTests extends AbstractKafkaTest {

    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;

    @Autowired
    ChatRoomRepository chatRoomRepository;

    @Before
    public void setup() {
        dbUnitHelper.deleteAllFixtures();

        dbUnitHelper.insertUsers();
        dbUnitHelper.insertUserContacts();
        dbUnitHelper.insertChatRooms();
        dbUnitHelper.insertChatRoomsToUsers();
    }

    @Test
    public void getChatRooms_userWithChatRooms_listOfChatRooms() {
        List<ChatRoom> chatRooms = userService.getChatRooms(getUser(1));

        assertThat(chatRooms, hasSize(1));
    }

    /**
     * Followed test is ignored due to metadata delay issue
     *
     * @throws AccessDeniedException
     */
    @Test
    @Ignore
    public void addChatRoom_ownerAndParticipants_chatRoomCreated() throws AccessDeniedException {
        User owner = getUser(1);
        User[] participants = getArrayOfUsers(2, 3);

        ChatRoom chatRoom = userService.addChatRoom(owner, participants);

        assertNotNull(chatRoomRepository.findOne(chatRoom.getTopic()));
        assertNotNull(chatRoom.getTopic());
        assertThat(chatRoom.getParticipants(), hasSize(3));
        assertThat(chatRoom.getParticipants(), containsInAnyOrder(owner, participants[0], participants[1]));

        messageService.sendMessage(getTestMessage(2, chatRoom.getTopic()));
        ConsumerRecord<Long, String> record = getRecord();

        assertThat(record, hasKey(2l));
        assertThat(record.topic(), is(chatRoom.getTopic()));
    }

    @Test(expected = AccessDeniedException.class)
    public void addChatRoom_participantNotFromContacts_exceptionThrown() throws AccessDeniedException {
        User owner = getUser(1);
        User[] participants = getArrayOfUsers(2, 3, 4);

        userService.addChatRoom(owner, participants);
    }

    @Test
    public void getContacts_userName_contactsReturned() {
        List<User> contacts = userService.getContacts(getUser(1));

        assertThat(contacts, containsInAnyOrder(getUser(2), getUser(3)));
    }
}