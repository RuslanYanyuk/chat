package com.chat.services;

import com.chat.models.Message;
import com.chat.models.Response;
import com.chat.models.ResponseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
@Service
public class MessageService {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public Response sendMessage(Message msg) {
        kafkaTemplate.send(msg.getChatRoom().getTopic(), msg.getSender().getId(), msg.getData());
        return new Response(ResponseType.SUCCESS);
    }
}
