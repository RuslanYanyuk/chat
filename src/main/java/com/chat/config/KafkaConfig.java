package com.chat.config;

import com.chat.models.ChatRoom;
import com.chat.models.Message;
import com.chat.utils.KafkaAdminUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static com.chat.config.WebSocketConfig.BROKER_PREFIX;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
@Configuration
@Profile("dev")
public class KafkaConfig {

    public static final String BROKER_ADDRESS = "localhost:9092";
    public static final String ZOOKEPER_HOST = "localhost:2181";
    public static final int SESSION_TIMEOUT = 1000;
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int PARTITIONS = 1;
    public static final int REPLICATION = 1;
    public static final boolean IS_SECURE_KAFKA_CLUSTER = false;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Map<String, Object> consumerProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_ADDRESS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "mainGroup");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 15000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 100);
        return props;
    }

    @Bean
    public ConsumerFactory<Long, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerProperties());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, String>
    kafkaListenerContainerFactory(ConsumerFactory<Long, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    public MessageListener<Long, String> messageListener() {
        return record -> {
            Message msg = new Message(record.value());
            msg.setChatRoom(new ChatRoom(record.topic()));
            messagingTemplate.convertAndSend(BROKER_PREFIX + "/" + record.topic(), msg);
        };
    }

    @Bean
    public KafkaMessageListenerContainer<Long, String>
    kafkaMessageListenerContainer(ConsumerFactory<Long, String> consumerFactory) {
        ContainerProperties containerProperties = new ContainerProperties(Pattern.compile(".*"));
        KafkaMessageListenerContainer<Long, String> container =
                new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setupMessageListener(messageListener());
        container.setBeanName("templateTests");
        container.start();
        return container;
    }

    public Map<String, Object> producerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_ADDRESS);
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<Long, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    public KafkaTemplate<Long, String> kafkaTemplate(ProducerFactory<Long, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ZkClient zkClient() {
        return new ZkClient(
                ZOOKEPER_HOST,
                SESSION_TIMEOUT,
                CONNECTION_TIMEOUT,
                ZKStringSerializer$.MODULE$);
    }

    @Bean
    public ZkUtils zkUtils(ZkClient zkClient) {
        return new ZkUtils(zkClient, new ZkConnection(ZOOKEPER_HOST), IS_SECURE_KAFKA_CLUSTER);
    }

    @Bean
    public KafkaAdminUtils kafkaAdminUtils(ZkUtils zkUtils) {
        return new KafkaAdminUtils(zkUtils, new Properties(), PARTITIONS, REPLICATION);
    }
}
