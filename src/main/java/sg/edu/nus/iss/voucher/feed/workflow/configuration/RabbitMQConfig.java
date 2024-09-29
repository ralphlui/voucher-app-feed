package sg.edu.nus.iss.voucher.feed.workflow.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue}")
    private String queue;

    @Value("${rabbitmq.routingkey}")
    private String routingKey;

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Queue feedQueue() {
        return new Queue(queue, true);
    }

    @Bean
    public Binding binding(Queue feedQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(feedQueue).to(topicExchange).with(routingKey);
    }
}

