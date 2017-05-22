package org.crazycoder.door.service;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DoorConfig {

    @Value("${door.service.queue}")
    private String doorServiceQueue;
    @Value("${door.service.topic}")
    private String doorServiceTopic;

    @Bean
    Queue queue() {
        return new Queue(doorServiceQueue, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(doorServiceTopic);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(doorServiceQueue);
    }
}
