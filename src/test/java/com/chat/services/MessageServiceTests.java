package com.chat.services;

import com.chat.common.AbstractKafkaTest;
import com.chat.exceptions.AccessDeniedException;
import com.chat.exceptions.MessageDeliveryException;
import com.chat.models.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.test.MessageFixtures.getTestMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasKey;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageServiceTests extends AbstractKafkaTest {

    @Autowired
    MessageService messageService;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void setup() {
        dbUnitHelper.deleteAllFixtures()
                .insertUsers()
                .insertChatRooms()
                .insertChatRoomsToUsers();
    }

    @Test
    public void sendMessage_messageAndRecipient_messageDelivered() throws AccessDeniedException, MessageDeliveryException, IOException {
        Message msg = getTestMessage(1, TEST_TOPIC);
        Message receivedMessage;
        ConsumerRecord<Long, String> received;

        messageService.sendMessage(msg);
        received = getRecord();
        receivedMessage = objectMapper.readValue(received.value(), Message.class);

        assertThat(received, hasKey(null));
        assertThat(receivedMessage.getData(), is(msg.getData()));
        assertThat(receivedMessage.getSender(), is(msg.getSender()));
        assertThat(receivedMessage.getChatRoom(), is(msg.getChatRoom()));
    }

    @Test(expected = AccessDeniedException.class)
    public void sendMessage_senderNotFromChat_exceptionThrown() throws AccessDeniedException, MessageDeliveryException {
        messageService.sendMessage(getTestMessage(4, TEST_TOPIC));
    }
}
