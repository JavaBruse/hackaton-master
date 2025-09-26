package com.javabruse.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {


    @Value("${topics.transfer-requests-task}")
    private String requestTask;
    @Value("${topics.transfer-response-task}")
    private String responseTask;

    @Bean
    public NewTopic taskRequest() {
        return new NewTopic(requestTask, 3, (short) 1);
    }

    @Bean
    public NewTopic taskResponse() {
        return new NewTopic(responseTask, 3, (short) 1);
    }
}
