package com.chat.services;

import com.chat.models.ChatRoom;
import com.chat.models.Message;
import com.chat.models.Sender;
import kafka.serializer.StringDecoder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.chat.services.matchers.ResponseMatcher.hasResponseSuccess;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasKey;
import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasValue;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MessagingTests {

    public static final String TEMPLATE_TOPIC = "templateTopic";
    @Autowired
    MessageService messageService;

    @Autowired
    ConsumerFactory<String, String> consumerFactory;

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, TEMPLATE_TOPIC);

    final BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();

    @Before
    public void setUp() throws Exception {
        ContainerProperties containerProperties = new ContainerProperties(TEMPLATE_TOPIC);
        KafkaMessageListenerContainer<String, String> container =
                new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setupMessageListener((MessageListener<String, String>) record -> {
            System.out.println(record);
            records.add(record);
        });
        container.setBeanName("templateTests");
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
    }

    @TestConfiguration
    static class TestKafkaConfig {

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, String>
        kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
            ConcurrentKafkaListenerContainerFactory<String, String> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            return factory;
        }

        @Bean
        public ConsumerFactory<String, String> consumerFactory() {
            Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testT", "false", embeddedKafka);
            consumerProps.replace(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            return new DefaultKafkaConsumerFactory<>(consumerProps);
        }

        public Map<String, Object> producerConfigs() {
            Map<String, Object> m = KafkaTestUtils.senderProps(embeddedKafka.getBrokersAsString());
            m.replace(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return m;
        }

        @Bean
        public ProducerFactory<String, String> producerFactory() {
            return new DefaultKafkaProducerFactory<>(producerConfigs());
        }

        @Bean
        public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }
    }

    @Test
    public void sendMessage_messageAndRecipient_responseSuccess() throws InterruptedException {
        Message msg = getTestMessage();
        ConsumerRecord<String, String> received;

        assertThat(messageService.sendMessage(msg), hasResponseSuccess());
        received = records.poll(10, TimeUnit.SECONDS);
        assertThat(received, hasKey(msg.getSender().getId()));
        assertThat(received, hasValue(msg.getData()));
    }

    private Message getTestMessage() {
        ChatRoom room = new ChatRoom(TEMPLATE_TOPIC);
        return new Message("Test message 1", getSender(), room);
    }

    private Sender getSender() {
        return new Sender("123-321-123");
    }
}
