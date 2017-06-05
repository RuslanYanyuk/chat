package com.chat.services;

import com.chat.common.AbstractKafkaTest;
import com.chat.exceptions.AccessDeniedException;
import com.chat.models.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.test.MessageFixtures.getTestMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasKey;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasValue;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageServiceTests extends AbstractKafkaTest {

    @Autowired
    MessageService messageService;

    @Before
    public void setup() {
        dbUnitHelper.deleteAllFixtures()
                .insertUsers()
                .insertChatRooms()
                .insertChatRoomsToUsers();
    }

    @Test
    public void sendMessage_messageAndRecipient_messageDelivered() throws AccessDeniedException {
        Message msg = getTestMessage(1, TEST_TOPIC);
        ConsumerRecord<Long, String> received;

        messageService.sendMessage(msg);
        received = getRecord();
        assertThat(received, hasKey(msg.getSender().getId()));
        assertThat(received, hasValue(msg.getData()));
    }

    @Test(expected = AccessDeniedException.class)
    public void sendMessage_senderNotFromChat_exceptionThrown() throws AccessDeniedException {
        messageService.sendMessage(getTestMessage(4, TEST_TOPIC));
    }
}
