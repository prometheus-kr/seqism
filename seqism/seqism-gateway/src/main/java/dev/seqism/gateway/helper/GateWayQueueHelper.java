package dev.seqism.gateway.helper;

import dev.seqism.common.constant.SeqismConstant;
import dev.seqism.common.helper.QueueNameHelper;
import dev.seqism.common.vo.ErrorInfo;
import dev.seqism.common.vo.SeqismException;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessageStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

/**
 * Helper class for managing RabbitMQ queues and message exchange within the gateway.
 * <p>
 * GateWayQueueHelper provides utility methods to:
 * <ul>
 * <li>Declare and delete command/response queues dynamically based on transaction IDs.</li>
 * <li>Send and receive {@link SeqismMessage} objects to/from specific queues, handling request/response patterns.</li>
 * <li>Enforce queue expiration and message receive timeouts as configured in application properties.</li>
 * <li>Handle AMQP errors and propagate them as {@link SeqismException} with appropriate error codes.</li>
 * </ul>
 * <p>
 * This class leverages {@link RabbitAdmin} for queue administration, {@link RabbitTemplate} for message operations,
 * and {@link QueueNameHelper} for consistent queue naming conventions.
 * <p>
 * Typical usage involves:
 * <ol>
 * <li>Creating queues for a transaction using {@link #createQueues(SeqismMessage)}.</li>
 * <li>Sending a message and waiting for a response using {@link #sendAndReceiveInit(SeqismMessage)} or
 * {@link #sendAndReceiveNext(SeqismMessage)}.</li>
 * <li>Automatically deleting queues when no longer needed or when message processing is complete.</li>
 * </ol>
 * <p>
 * Queue expiration and message receive timeouts are configurable via application properties:
 * <ul>
 * <li><b>seqism.queue.delete.timeout</b>: Queue expiration time in milliseconds (default:
 * {@link SeqismConstant#QUEUE_DELETE_TIME}).</li>
 * <li><b>seqism.queue.receive.timeout</b>: Message receive timeout in milliseconds (default:
 * {@link SeqismConstant#RECEIVE_TIME_OUT}).</li>
 * </ul>
 * <p>
 * All operations are logged for debugging and traceability.
 */
@Slf4j
@Component
public class GateWayQueueHelper {
    /**
     * An instance of {@link RabbitAdmin} used to manage AMQP resources such as queues, exchanges, and bindings
     * within the RabbitMQ broker. This helper facilitates administrative operations like declaring or deleting
     * queues and exchanges programmatically.
     */
    private final RabbitAdmin rabbitAdmin;
    /**
     * The {@code RabbitTemplate} instance used for sending and receiving messages
     * to and from RabbitMQ queues. This template simplifies RabbitMQ operations
     * such as publishing and consuming messages within the gateway.
     */
    private final RabbitTemplate rabbitTemplate;
    /**
     * Helper class responsible for generating and managing queue names within the gateway.
     * Used to ensure consistent naming conventions for message queues.
     */
    private final QueueNameHelper queueNameHelper;
    /**
     * Queue expiration time (ms), loaded from application properties.
     * If not set, defaults to SeqismConstant.QUEUE_DELETE_TIME.
     */
    private final long queueDeleteTimeout;
    /**
     * The maximum time in milliseconds to wait for a message to be received from the queue
     * before timing out. A value of zero indicates an immediate timeout, while a negative
     * value may indicate an indefinite wait, depending on implementation.
     */
    private final long messageReceiveTimeout;

    /**
     * Constructs a new {@code GateWayQueueHelper} instance with the specified dependencies and configuration values.
     *
     * @param rabbitAdmin
     *            the {@link RabbitAdmin} instance used for managing AMQP resources.
     * @param rabbitTemplate
     *            the {@link RabbitTemplate} instance used for sending and receiving messages.
     * @param queueNameHelper
     *            the {@link QueueNameHelper} used for generating and managing queue names.
     * @param queueDeleteTimeout
     *            the timeout (in milliseconds) for deleting queues, injected from the property
     *            {@code seqism.queue.delete.timeout} or defaults to {@code SeqismConstant.QUEUE_DELETE_TIME}.
     * @param messageReceiveTimeout
     *            the timeout (in milliseconds) for receiving messages, injected from the property
     *            {@code seqism.queue.receive.timeout} or defaults to {@code SeqismConstant.RECEIVE_TIME_OUT}.
     */
    public GateWayQueueHelper(
            RabbitAdmin rabbitAdmin,
            RabbitTemplate rabbitTemplate,
            QueueNameHelper queueNameHelper,
            @Value("${seqism.queue.delete.timeout:" + SeqismConstant.QUEUE_DELETE_TIME + "}") long queueDeleteTimeout,
            @Value("${seqism.queue.receive.timeout:" + SeqismConstant.RECEIVE_TIME_OUT
                    + "}") long messageReceiveTimeout) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
        this.queueNameHelper = queueNameHelper;
        this.queueDeleteTimeout = queueDeleteTimeout;
        this.messageReceiveTimeout = messageReceiveTimeout;
    }

    /**
     * Sends the provided {@link SeqismMessage} to a static queue and waits for a response.
     * <p>
     * This method performs the following steps:
     * <ol>
     * <li>Logs the outgoing message for debugging purposes.</li>
     * <li>Creates the necessary queues for message processing.</li>
     * <li>Sends the message to a static queue determined by {@code queueNameHelper}.</li>
     * <li>Waits for and returns the response message.</li>
     * </ol>
     *
     * @param <R>
     *            the type of the response message payload
     * @param <C>
     *            the type of the command message payload
     * @param message
     *            the message to send
     * @return the response message received
     */

    public <R, C> SeqismMessage<C> sendAndReceiveInit(SeqismMessage<R> message) {
        log.debug("Sending message : [{}]", message);

        createQueues(message);

        send(queueNameHelper.getStaticQueueName(), message);
        return receive(message);
    }

    /**
     * Sends the specified {@link SeqismMessage} to a response queue determined by the transaction ID,
     * then waits to receive the next message in response.
     *
     * <p>
     * This method logs the outgoing message, sends it to the appropriate response queue,
     * and then calls {@link #receive(SeqismMessage)} to wait for and return the next message.
     *
     * @param <R>
     *            the type of the response message payload
     * @param <C>
     *            the type of the command message payload
     * @param message
     *            the message to send and await a response for
     * @return the next {@link SeqismMessage} received in response
     */
    public <R, C> SeqismMessage<C> sendAndReceiveNext(SeqismMessage<R> message) {
        log.debug("Sending message : [{}]", message);
        String tranId = message.getHeader().getTranId();

        send(queueNameHelper.getResponseQueueName(tranId), message);
        return receive(message);
    }

    /**
     * Creates command and response queues for the given message transaction.
     * <p>
     * This method generates queue names based on the transaction ID extracted from the message header,
     * declares both the command and response queues, and logs their creation.
     *
     * @param message
     *            the {@link SeqismMessage} containing the transaction information used to generate queue names
     */
    void createQueues(SeqismMessage<?> message) {
        String tranId = message.getHeader().getTranId();

        String commandQueue = queueNameHelper.getCommandQueueName(tranId);
        String responseQueue = queueNameHelper.getResponseQueueName(tranId);

        declareQueue(commandQueue);
        declareQueue(responseQueue);
        log.debug("Created queues : [{}], [{}]", commandQueue, responseQueue);
    }

    /**
     * Declares a durable queue with the specified name and sets its expiration time.
     * The queue will be automatically deleted after the period defined by {@code SeqismConstant.QUEUE_DELETE_TIME}.
     *
     * @param queueName
     *            the name of the queue to declare
     */
    void declareQueue(String queueName) {
        Queue queue = QueueBuilder.durable(queueName)
                .withArgument("x-expires", this.queueDeleteTimeout)
                .build();
        rabbitAdmin.declareQueue(queue);
    }

    /**
     * Sends a {@link SeqismMessage} to the specified queue.
     * <p>
     * Checks if the queue exists before sending the message. If the queue does not exist,
     * throws a {@link SeqismException} with {@link ErrorInfo#ERROR_0001_0003}. If an AMQP-related
     * error occurs during sending, throws a {@link SeqismException} with {@link ErrorInfo#ERROR_0001_0004}.
     *
     * @param queueName
     *            the name of the target queue
     * @param message
     *            the message to send
     * @param <T>
     *            the type of the message payload
     * @throws SeqismException
     *             if the queue does not exist or if an AMQP error occurs
     */
    @SuppressWarnings("unused")
    <T> void send(String queueName, SeqismMessage<T> message) {
        try {
            if (rabbitAdmin.getQueueProperties(queueName) == null) {
                throw new SeqismException(ErrorInfo.ERROR_0001_0003);
            }

            rabbitTemplate.convertAndSend(queueName, message);
        } catch (AmqpException e) {
            throw new SeqismException(ErrorInfo.ERROR_0001_0004, e);
        }
    }

    /**
     * Receives a message from the specified command queue using the provided {@link SeqismMessage} as a reference.
     * Attempts to convert the received message to the appropriate type using a {@link ParameterizedTypeReference}.
     * If no message is received within the timeout or the message status is not {@code IN_PROGRESS},
     * deletes both the command and response queues associated with the transaction ID.
     * 
     * @param <R> the type of the response message payload
     * @param <C> the type of the command message payload
     * @param message
     *            the reference message containing the transaction ID and header information
     * @return the received {@link SeqismMessage} of type {@code T}, or {@code null} if no message was received
     * @throws SeqismException
     *             if an AMQP error occurs during message reception
     */
    <R, C> SeqismMessage<C> receive(SeqismMessage<R> message) {
        String tranId = message.getHeader().getTranId();

        String commandQueue = queueNameHelper.getCommandQueueName(tranId);
        String responseQueue = queueNameHelper.getResponseQueueName(tranId);

        ParameterizedTypeReference<SeqismMessage<C>> typeRef = new ParameterizedTypeReference<SeqismMessage<C>>() {
        };

        try {
            SeqismMessage<C> receivedMsg //
                    = rabbitTemplate.receiveAndConvert(commandQueue, this.messageReceiveTimeout, typeRef);
            log.debug("Received message : [{}]", receivedMsg);

            if (receivedMsg == null || receivedMsg.getHeader().getStatus() != SeqismMessageStatus.IN_PROGRESS) {
                rabbitAdmin.deleteQueue(commandQueue);
                rabbitAdmin.deleteQueue(responseQueue);
                log.debug("Deleted queues : [{}], [{}]", commandQueue, responseQueue);
            }

            return receivedMsg;
        } catch (AmqpException e) {
            throw new SeqismException(ErrorInfo.ERROR_0001_0005, e);
        }
    }
}