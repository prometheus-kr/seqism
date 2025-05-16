package dev.seqism.gateway;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitConfig
 *
 * @author seqism
 * @since 2025.05.16
 */
@Configuration
public class RabbitConfig {

    @Bean
    public Queue requestQueue() {
        return new Queue("seqism-request-queue", true);
    }

    @Bean
    public Queue responseQueue() {
        return new Queue("seqism-response-queue", true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange("seqism-exchange");
    }

    @Bean
    public Binding binding(Queue requestQueue, DirectExchange exchange) {
        return BindingBuilder.bind(requestQueue).to(exchange).with("seqism-request-routing-key");
    }

    @Bean
    public Binding responseBinding(Queue responseQueue, DirectExchange exchange) {
        return BindingBuilder.bind(responseQueue).to(exchange).with("seqism-response-routing-key");
    }
}