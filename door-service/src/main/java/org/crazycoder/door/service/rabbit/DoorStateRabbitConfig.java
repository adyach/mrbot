package org.crazycoder.door.service.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DoorStateRabbitConfig {

    @Bean
    public Queue queue(@Value("${rpc.doors.queue:rpc.doors.state}") String queueName) {
        return new Queue(queueName, false);
    }

    @Bean
    public DirectExchange exchange(@Value("${rpc.doors.exchange:rpc.doors}") String exchangeName) {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("rpc.doors.state");
    }

}
