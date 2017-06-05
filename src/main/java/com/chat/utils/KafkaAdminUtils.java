package com.chat.utils;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZkUtils;

import java.util.Properties;

/**
 * @author Ruslan Yaniuk
 * @date June 2017
 */
public class KafkaAdminUtils {

    private ZkUtils zkUtils;
    private Properties topicConfig;
    private int partitions;
    private int replication;

    public KafkaAdminUtils(ZkUtils zkUtils, Properties topicConfig, int partitions, int replication) {
        this.zkUtils = zkUtils;
        this.topicConfig = topicConfig;
        this.partitions = partitions;
        this.replication = replication;
    }

    public void crateTopic(String topic) {
        AdminUtils.createTopic(zkUtils, topic, partitions, replication, topicConfig, RackAwareMode.Enforced$.MODULE$);
    }

    public void deleteTopic(String topic) {
        AdminUtils.deleteTopic(zkUtils, topic);
    }

    public boolean topicExists(String topic) {
        return AdminUtils.topicExists(zkUtils, topic);
    }
}
