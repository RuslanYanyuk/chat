package com.chat.common;

import com.chat.config.KafkaConfig;
import com.chat.utils.KafkaAdminUtils;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

/**
 * @author Ruslan Yaniuk
 * @date June 2017
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class AbstractKafkaTest extends AbstractTest {

    public static final String TEST_TOPIC = "f4938dbe-bc98-4ca1-b38c-1279b274d8c1";
    public static final BlockingQueue<ConsumerRecord<Long, String>> records = new LinkedBlockingQueue<>();

    @Autowired
    public KafkaMessageListenerContainer<Long, String> kafkaMessageListenerContainer;

    @Autowired
    public KafkaEmbedded embeddedKafka;

    @Before
    public void setUpKafkaTest() throws Exception {
        records.clear();
        waitForAssignment(kafkaMessageListenerContainer, embeddedKafka.getPartitionsPerTopic());
    }

    @TestConfiguration
    public static class KafkaTestConfig extends KafkaConfig {

        @Autowired
        public KafkaEmbedded embeddedKafka;

        @Bean(initMethod = "before", destroyMethod = "after")
        public KafkaEmbedded kafkaEmbedded() {
            return new KafkaEmbedded(1, true, TEST_TOPIC);
        }

        @Override
        public Map<String, Object> consumerProperties() {
            Map<String, Object> props = super.consumerProperties();
            props.replace(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
            return props;
        }

        @Override
        public Map<String, Object> producerConfig() {
            Map<String, Object> props = super.producerConfig();
            props.replace(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
            return props;
        }

        @Override
        public ZkClient zkClient() {
            return embeddedKafka.getZkClient();
        }

        @Override
        public ZkUtils zkUtils(ZkClient zkClient) {
            String zooKeeperHost = "127.0.0.1:" + embeddedKafka.getZookeeper().port();
            return new ZkUtils(zkClient, new ZkConnection(zooKeeperHost), IS_SECURE_KAFKA_CLUSTER);
        }

        @Override
        public MessageListener<Long, String> messageListener() {
            final MessageListener<Long, String> def = super.messageListener();
            return data -> {
                def.onMessage(data);
                records.add(data);
            };
        }

        @Override
        public KafkaAdminUtils kafkaAdminUtils(ZkUtils zkUtils) {
            return new KafkaAdminUtils(zkUtils, new Properties(), embeddedKafka.getPartitionsPerTopic(), REPLICATION);
        }
    }

    public static ConsumerRecord<Long, String> getRecord() {
        try {
            return records.poll(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
