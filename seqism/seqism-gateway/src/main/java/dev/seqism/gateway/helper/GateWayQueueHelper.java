package dev.seqism.gateway.helper;

import dev.seqism.common.constant.SeqismConstant;
import dev.seqism.common.helper.QueueNameHelper;
import dev.seqism.common.vo.ErrorInfo;
import dev.seqism.common.vo.SeqismException;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessageStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

/**
 * Helper component for managing RabbitMQ queues and message exchange in the gateway.
 * <p>
 * This class provides methods to:
 * <ul>
 * <li>Create command and response queues for a transaction.</li>
 * <li>Send and receive messages synchronously using RabbitMQ.</li>
 * <li>Handle queue declaration and deletion based on message status.</li>
 * </ul>
 * 
 * <p>
 * Usage:
 * <ul>
 * <li>{@link #sendAndReceiveInit(SeqismMessage)} - Initializes queues and sends the initial message.</li>
 * <li>{@link #sendAndReceiveNext(SeqismMessage)} - Sends a message to the response queue and waits for a reply.</li>
 * </ul>
 * 
 * <p>
 * Queues are automatically deleted if a message is not received within the timeout or if the message status is not
 * {@code IN_PROGRESS}.
 * 
 * <p>
 * Exceptions are wrapped in {@link SeqismException} with appropriate error codes.
 * 
 * @see RabbitAdmin
 * @see RabbitTemplate
 * @see SeqismMessage
 * @see QueueNameHelper
 */
@Slf4j
@Component
@AllArgsConstructor
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
     * Sends the given {@link SeqismMessage} to a static queue and waits for a response.
     * <p>
     * This method performs the following steps:
     * <ol>
     * <li>Logs the outgoing message for debugging purposes.</li>
     * <li>Creates the necessary queues for the message.</li>
     * <li>Sends the message to a static queue.</li>
     * <li>Waits for and returns the response message.</li>
     * </ol>
     *
     * @param <T>
     *            the type of the payload contained in the {@link SeqismMessage}
     * @param message
     *            the message to send and receive a response for
     * @return the response {@link SeqismMessage} received after sending the original message
     */
    public <T> SeqismMessage<T> sendAndReceiveInit(SeqismMessage<T> message) {
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
     * @param <T>
     *            the type of the payload contained in the {@link SeqismMessage}
     * @param message
     *            the message to send and await a response for
     * @return the next {@link SeqismMessage} received in response
     */
    public <T> SeqismMessage<T> sendAndReceiveNext(SeqismMessage<T> message) {
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
                .withArgument("x-expires", SeqismConstant.QUEUE_DELETE_TIME)
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
     * @param <T>
     *            the type of the payload in the {@link SeqismMessage}
     * @param message
     *            the reference message containing the transaction ID and header information
     * @return the received {@link SeqismMessage} of type {@code T}, or {@code null} if no message was received
     * @throws SeqismException
     *             if an AMQP error occurs during message reception
     */
    <T> SeqismMessage<T> receive(SeqismMessage<T> message) {
        String tranId = message.getHeader().getTranId();

        String commandQueue = queueNameHelper.getCommandQueueName(tranId);
        String responseQueue = queueNameHelper.getResponseQueueName(tranId);

        ParameterizedTypeReference<SeqismMessage<T>> typeRef = new ParameterizedTypeReference<SeqismMessage<T>>() {
        };

        try {
            SeqismMessage<T> receivedMsg //
                    = rabbitTemplate.receiveAndConvert(commandQueue, SeqismConstant.RECEIVE_TIME_OUT, typeRef);
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