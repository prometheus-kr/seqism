package dev.seqism.gateway.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.seqism.common.helper.QueueNameHelper;

/**
 * Configuration class for setting up RabbitMQ integration in the application.
 * <p>
 * This class defines beans for:
 * <ul>
 * <li>{@link Jackson2JsonMessageConverter} - for serializing and deserializing messages as JSON.</li>
 * <li>{@link RabbitTemplate} - the main interface for sending and receiving messages.</li>
 * <li>{@link RabbitAdmin} - for declaring queues, exchanges, and bindings programmatically.</li>
 * <li>{@link Queue} - the application's static queue, named by {@code SeqismConstant.SEQISM_STATIC_QUEUE}.</li>
 * </ul>
 * <p>
 * The class is annotated with {@code @Configuration} to indicate that it provides Spring beans,
 * and {@code @EnableRabbit} to enable support for RabbitMQ messaging.
 */
@Configuration
@EnableRabbit
public class RabbitConfig {
    /**
     * Creates a {@link Jackson2JsonMessageConverter} bean for converting messages to and from JSON
     * using the Jackson library. This converter is typically used with Spring AMQP to automatically
     * serialize and deserialize message payloads.
     *
     * @return a configured {@link Jackson2JsonMessageConverter} instance
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Creates and configures a {@link RabbitTemplate} bean with the provided {@link ConnectionFactory}
     * and {@link Jackson2JsonMessageConverter}. This template is used for sending and receiving messages
     * to and from RabbitMQ, with message conversion handled by Jackson for JSON serialization and deserialization.
     *
     * @param connectionFactory
     *            the connection factory to connect to RabbitMQ
     * @param jackson2JsonMessageConverter
     *            the message converter for JSON serialization
     * @return a configured {@link RabbitTemplate} instance
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter);
        return template;
    }

    /**
     * Creates and configures a {@link RabbitAdmin} bean using the provided {@link RabbitTemplate}.
     * <p>
     * {@code RabbitAdmin} is a helper class that simplifies the management of AMQP resources such as queues,
     * exchanges, and bindings within a RabbitMQ broker. By declaring this bean, the application can automatically
     * declare and manage these resources at startup.
     *
     * @param rabbitTemplate
     *            the {@link RabbitTemplate} to be used by the {@code RabbitAdmin} for communication with RabbitMQ
     * @return a configured {@link RabbitAdmin} instance
     */
    @Bean
    public RabbitAdmin rabbitAdmin(RabbitTemplate rabbitTemplate) {
        return new RabbitAdmin(rabbitTemplate);
    }

    /**
     * Defines a RabbitMQ queue bean with a configurable name.
     * <p>
     * The queue name is injected from the application properties using the key
     * {@code seqism.queue.static.name}. If the property is not set, it defaults to
     * {@code seqism-static-queue}.
     *
     * @param queueName
     *            the name of the queue, resolved from application properties
     * @return a new {@link Queue} instance with the specified name
     */
    @Bean
    public Queue queue(@Value("${seqism.queue.static.name:seqism-static-queue}") String queueName) {
        return new Queue(queueName);
    }

    /**
     * Creates and configures a {@link QueueNameHelper} bean with queue name properties.
     *
     * @param staticQueueName
     *            the name of the static queue, defaults to "seqism-static-queue" if not specified
     * @param commandQueuePrefix
     *            the prefix for command queues, defaults to "seqism-command-queue." if not specified
     * @param responseQueuePrefix
     *            the prefix for response queues, defaults to "seqism-response-queue." if not specified
     * @return a configured {@link QueueNameHelper} instance
     */
    @Bean
    public QueueNameHelper queueNameHelper(
            @Value("${seqism.queue.static.name:seqism-static-queue}") String staticQueueName,
            @Value("${seqism.queue.command.prefix:seqism-command-queue.}") String commandQueuePrefix,
            @Value("${seqism.queue.response.prefix:seqism-response-queue.}") String responseQueuePrefix) {
        return new QueueNameHelper(staticQueueName, commandQueuePrefix, responseQueuePrefix);
    }
}