package com.chat.services;

import com.chat.exceptions.AccessDeniedException;
import com.chat.exceptions.MessageDeliveryException;
import com.chat.models.ChatRoom;
import com.chat.models.Message;
import com.chat.models.User;
import com.chat.utils.KafkaHistoryThreadFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
@Service
public class MessageService {

    public static final String NOT_PARTICIPANT = "User is not participant of the chat room";
    public static final ThreadPoolExecutor POOL_EXECUTOR = new ThreadPoolExecutor(0, 100,
            Integer.MAX_VALUE, TimeUnit.DAYS,
            new SynchronousQueue<>(), new KafkaHistoryThreadFactory());

    @Autowired
    KafkaTemplate<Long, String> kafkaTemplate;

    @Autowired
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Resource(name = "consumerProperties")
    Map<String, Object> consumerProperties;

    public void sendMessage(Message msg) throws AccessDeniedException, MessageDeliveryException {
        User sender = msg.getSender();
        ChatRoom chatRoom = new ChatRoom(msg.getChatRoom().getTopic());

        if (!userService.getChatRooms(sender).contains(chatRoom)) { //TODO implement cache
            throw new AccessDeniedException(NOT_PARTICIPANT);
        }
        kafkaTemplate.send(chatRoom.getTopic(), System.currentTimeMillis(), serialize(msg));
    }

    public String serialize(Message msg) throws MessageDeliveryException {
        try {
            return objectMapper.writeValueAsString(msg);
        } catch (JsonProcessingException e) {
            throw new MessageDeliveryException("Could not convert the message", e);
        }
    }

    public void getAllMessages(String topic, String username) {
        POOL_EXECUTOR.execute(new MessageHistoryReader(consumerProperties, topic, messagingTemplate, username));
    }

    public static class MessageHistoryReader implements Runnable {

        public static final int READ_TIMEOUT = 1000;

        private Map<String, Object> cp;
        private String topic;
        private SimpMessageSendingOperations smso;
        private String username;

        public MessageHistoryReader(Map<String, Object> consumerProperties, String topic,
                                    SimpMessageSendingOperations smso, String username) {
            this.cp = consumerProperties;
            this.topic = topic;
            this.smso = smso;
            this.username = username;
        }

        @Override
        public void run() {
            cp.replace(ConsumerConfig.GROUP_ID_CONFIG, Thread.currentThread().getName());
            Consumer<Long, String> consumer = new KafkaConsumer<>(cp);
            List<String> messages = new ArrayList<>();
            TopicPartition tp = new TopicPartition(topic, 0);
            List<TopicPartition> list = Collections.singletonList(tp);

            consumer.assign(list);
            consumer.seekToBeginning(list);
            consumer.poll(READ_TIMEOUT).records(tp).forEach(r -> messages.add(r.value()));
            smso.convertAndSendToUser(username, "/topic/chat-history", messages);
            consumer.close();
        }
    }
}