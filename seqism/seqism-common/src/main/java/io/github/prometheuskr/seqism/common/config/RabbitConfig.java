package io.github.prometheuskr.seqism.common.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.prometheuskr.seqism.common.constant.SeqismConstant;
import io.github.prometheuskr.seqism.common.helper.QueueNameHelper;

/**
 * RabbitMQ configuration for the Seqism framework.
 * <p>
 * Defines beans for JSON message conversion, {@link RabbitTemplate} (with configurable timeouts),
 * {@link RabbitAdmin}, static/onetime queues, and {@link QueueNameHelper}.
 * <p>
 * Configurable application properties:
 * <ul>
 * <li>{@code seqism.queue.static.name} — Static queue name (default: {@link SeqismConstant#SEQISM_STATIC_QUEUE})</li>
 * <li>{@code seqism.queue.onetime.name} — Onetime queue name (default: {@link SeqismConstant#SEQISM_ONETIME_QUEUE})</li>
 * <li>{@code seqism.queue.command.prefix} — Command queue prefix (default: {@link SeqismConstant#COMMAND_QUEUE_PREFIX})</li>
 * <li>{@code seqism.queue.response.prefix} — Response queue prefix (default: {@link SeqismConstant#RESPONSE_QUEUE_PREFIX})</li>
 * <li>{@code seqism.queue.receive.timeout} — Receive/reply timeout in ms (default: {@link SeqismConstant#RECEIVE_TIME_OUT})</li>
 * </ul>
 */
@Configuration
@EnableRabbit
public class RabbitConfig {
    /** Default constructor. */
    public RabbitConfig() {}

    /**
     * JSON message converter using Jackson.
     *
     * @return a {@link Jackson2JsonMessageConverter} instance
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * {@link RabbitTemplate} with JSON conversion and configurable receive/reply timeouts.
     *
     * @param connectionFactory
     *            the connection factory
     * @param jackson2JsonMessageConverter
     *            the JSON message converter
     * @param messageReceiveTimeout
     *            receive and reply timeout in ms ({@code seqism.queue.receive.timeout})
     * @return a configured {@link RabbitTemplate} instance
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter,
            @Value("${seqism.queue.receive.timeout:" + SeqismConstant.RECEIVE_TIME_OUT
                    + "}") long messageReceiveTimeout) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter);
        template.setReplyTimeout(messageReceiveTimeout);
        template.setReceiveTimeout(messageReceiveTimeout);
        return template;
    }

    /**
     * {@link RabbitAdmin} for managing AMQP resources (queues, exchanges, bindings).
     *
     * @param rabbitTemplate
     *            the template for RabbitMQ communication
     * @return a configured {@link RabbitAdmin} instance
     */
    @Bean
    public RabbitAdmin rabbitAdmin(RabbitTemplate rabbitTemplate) {
        return new RabbitAdmin(rabbitTemplate);
    }

    /**
     * Static queue for multi-step message processing.
     *
     * @param queueName
     *            queue name ({@code seqism.queue.static.name})
     * @return a new {@link Queue} instance
     */
    @Bean
    public Queue staticQueue(
            @Value("${seqism.queue.static.name:" + SeqismConstant.SEQISM_STATIC_QUEUE + "}") String queueName) {
        return new Queue(queueName);
    }

    /**
     * Onetime queue for RPC-style single-transaction exchange.
     *
     * @param queueName
     *            queue name ({@code seqism.queue.onetime.name})
     * @return a new {@link Queue} instance
     */
    @Bean
    public Queue onetimeQueue(
            @Value("${seqism.queue.onetime.name:" + SeqismConstant.SEQISM_ONETIME_QUEUE + "}") String queueName) {
        return new Queue(queueName);
    }

    /**
     * {@link QueueNameHelper} configured with queue names and prefixes from application properties.
     *
     * @param staticQueueName
     *            static queue name ({@code seqism.queue.static.name})
     * @param commandQueuePrefix
     *            command queue prefix ({@code seqism.queue.command.prefix})
     * @param responseQueuePrefix
     *            response queue prefix ({@code seqism.queue.response.prefix})
     * @param onetimeQueueName
     *            onetime queue name ({@code seqism.queue.onetime.name})
     * @return a configured {@link QueueNameHelper} instance
     */
    @Bean
    public QueueNameHelper queueNameHelper(
            @Value("${seqism.queue.static.name:" + SeqismConstant.SEQISM_STATIC_QUEUE + "}") String staticQueueName,
            @Value("${seqism.queue.command.prefix:" + SeqismConstant.COMMAND_QUEUE_PREFIX
                    + "}") String commandQueuePrefix,
            @Value("${seqism.queue.response.prefix:" + SeqismConstant.RESPONSE_QUEUE_PREFIX
                    + "}") String responseQueuePrefix,
            @Value("${seqism.queue.onetime.name:" + SeqismConstant.SEQISM_ONETIME_QUEUE
                    + "}") String onetimeQueueName) {
        return new QueueNameHelper(staticQueueName, commandQueuePrefix, responseQueuePrefix, onetimeQueueName);
    }
}
