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

import dev.seqism.common.constant.SeqismConstant;
import dev.seqism.common.helper.QueueNameHelper;

/**
 * Configuration class for setting up RabbitMQ integration using Spring AMQP.
 * <p>
 * This class defines beans for message conversion, RabbitMQ template, administration,
 * queue declaration, and queue name management. It leverages Jackson for JSON
 * serialization/deserialization of messages and provides configurable queue names
 * and prefixes via application properties.
 * <ul>
 * <li>Configures a {@link Jackson2JsonMessageConverter} for JSON message conversion.</li>
 * <li>Defines a {@link RabbitTemplate} for sending and receiving messages with JSON support.</li>
 * <li>Provides a {@link RabbitAdmin} for managing AMQP resources such as queues and exchanges.</li>
 * <li>Declares a configurable RabbitMQ {@link Queue} bean.</li>
 * <li>Creates a {@link QueueNameHelper} bean for managing queue names and prefixes.</li>
 * </ul>
 * <p>
 * Queue names and prefixes can be customized via the following application properties:
 * <ul>
 * <li><code>seqism.queue.static.name</code> (defaults to {@link SeqismConstant#SEQISM_STATIC_QUEUE})</li>
 * <li><code>seqism.queue.command.prefix</code> (defaults to {@link SeqismConstant#COMMAND_QUEUE_PREFIX})</li>
 * <li><code>seqism.queue.response.prefix</code> (defaults to {@link SeqismConstant#RESPONSE_QUEUE_PREFIX})</li>
 * </ul>
 */
@Configuration
@EnableRabbit
public class RabbitConfig {
    /**
     * Default constructor for the {@code RabbitConfig} class.
     * Initializes a new instance of the configuration without any parameters.
     */
    public RabbitConfig() {}

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
    public Queue queue(
            @Value("${seqism.queue.static.name:" + SeqismConstant.SEQISM_STATIC_QUEUE + "}") String queueName) {
        return new Queue(queueName);
    }

    /**
     * Creates a {@link QueueNameHelper} bean configured with queue names and prefixes.
     *
     * @param staticQueueName
     *            the name of the static queue, resolved from the property
     *            {@code seqism.queue.static.name} or defaults to {@link SeqismConstant#SEQISM_STATIC_QUEUE}
     * @param commandQueuePrefix
     *            the prefix for command queues, resolved from the property
     *            {@code seqism.queue.command.prefix} or defaults to {@link SeqismConstant#COMMAND_QUEUE_PREFIX}
     * @param responseQueuePrefix
     *            the prefix for response queues, resolved from the property
     *            {@code seqism.queue.response.prefix} or defaults to {@link SeqismConstant#RESPONSE_QUEUE_PREFIX}
     * @return a configured {@link QueueNameHelper} instance
     */
    @Bean
    public QueueNameHelper queueNameHelper(
            @Value("${seqism.queue.static.name:" + SeqismConstant.SEQISM_STATIC_QUEUE + "}") String staticQueueName,
            @Value("${seqism.queue.command.prefix:" + SeqismConstant.COMMAND_QUEUE_PREFIX
                    + "}") String commandQueuePrefix,
            @Value("${seqism.queue.response.prefix:" + SeqismConstant.RESPONSE_QUEUE_PREFIX
                    + "}") String responseQueuePrefix) {
        return new QueueNameHelper(staticQueueName, commandQueuePrefix, responseQueuePrefix);
    }
}