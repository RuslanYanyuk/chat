package com.chat.utils;

import com.chat.models.Message;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * @author Ruslan Yaniuk
 * @date October 2017
 */
public class MessageSerializer extends JsonSerializer<Message> {

    @Override
    public void serialize(Message value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("data", value.getData());
        gen.writeStringField("timestamp", value.getTimestamp().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        gen.writeObjectFieldStart("sender");
        gen.writeStringField("name", value.getSender().getName());
        gen.writeEndObject();
        gen.writeObjectFieldStart("chatRoom");
        gen.writeStringField("topic", value.getChatRoom().getTopic());
        gen.writeEndObject();
    }
}
