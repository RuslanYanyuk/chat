package com.test;

import com.chat.models.ChatRoom;
import com.chat.models.Message;

import static com.test.UserFixtures.getUser;

/**
 * @author Ruslan Yaniuk
 * @date June 2017
 */
public class MessageFixtures {

    public static Message getTestMessage(int i, String topic) {
        Message msg = new Message("Test message " + i);
        msg.setSender(getUser(i));
        msg.setChatRoom(new ChatRoom(topic));
        return msg;
    }
}
