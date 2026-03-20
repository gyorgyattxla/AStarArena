package com.astar.arena.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME = "judge_queue";
    public static final String EXCHANGE_NAME = "judge_exchange";
    public static final String ROUTING_KEY = "judge_routing_key";

    @Bean
    public Queue judgeQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange judgeExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue judgeQueue, DirectExchange judgeExchange) {
        return BindingBuilder.bind(judgeQueue).to(judgeExchange).with(ROUTING_KEY);
    }
}