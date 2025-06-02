package io.github.prometheuskr.seqism.processor.helper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import io.github.prometheuskr.seqism.common.constant.SeqismConstant;
import io.github.prometheuskr.seqism.common.helper.QueueNameHelper;
import io.github.prometheuskr.seqism.common.vo.ErrorInfo;
import io.github.prometheuskr.seqism.common.vo.SeqismException;
import io.github.prometheuskr.seqism.common.vo.SeqismMessage;

/**
 * Helper component for managing message sending and receiving operations with RabbitMQ queues
 * in the processor module. This class provides utility methods for sending messages to command
 * queues and receiving responses from response queues, handling queue name resolution and
 * message timeouts.
 * <p>
 * Dependencies such as {@link RabbitTemplate} and {@link QueueNameHelper} are injected to
 * facilitate message operations and queue name management. The message receive timeout is
 * configurable via application properties.
 * <p>
 * Main responsibilities:
 * <ul>
 * <li>Sending {@link SeqismMessage} instances to the appropriate command queue.</li>
 * <li>Receiving {@link SeqismMessage} responses from the corresponding response queue,
 * with timeout and error handling.</li>
 * <li>Providing a combined send-and-receive operation for synchronous messaging patterns.</li>
 * </ul>
 * <p>
 * Exceptions encountered during message operations are wrapped and rethrown as
 * {@link SeqismException} with appropriate error information.
 */
@Slf4j
@Component
public class ProcessorQueueHelper {
    /**
     * The {@code RabbitTemplate} instance used for sending and receiving messages
     * to and from a RabbitMQ message broker. This template provides convenient
     * methods for interacting with RabbitMQ, such as converting and sending
     * messages, as well as receiving and converting messages.
     */
    private final RabbitTemplate rabbitTemplate;
    /**
     * Helper instance for managing and generating queue names within the processor.
     */
    private final QueueNameHelper queueNameHelper;
    /**
     * The maximum time in milliseconds to wait for a message to be received from the queue
     * before timing out. A value of zero indicates an immediate timeout, while a negative
     * value may indicate an indefinite wait, depending on implementation.
     */
    private final long messageReceiveTimeout;

    /**
     * Constructs a new {@code ProcessorQueueHelper} with the specified dependencies.
     *
     * @param rabbitTemplate
     *            the {@link RabbitTemplate} used for sending and receiving messages from RabbitMQ
     * @param queueNameHelper
     *            the {@link QueueNameHelper} used for resolving queue names
     * @param messageReceiveTimeout
     *            the timeout value (in milliseconds) for receiving messages from the queue,
     *            injected from the application properties or defaults to {@link SeqismConstant#RECEIVE_TIME_OUT}
     */
    public ProcessorQueueHelper(
            RabbitTemplate rabbitTemplate,
            QueueNameHelper queueNameHelper,
            @Value("${seqism.queue.receive.timeout:" + SeqismConstant.RECEIVE_TIME_OUT
                    + "}") long messageReceiveTimeout) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueNameHelper = queueNameHelper;
        this.messageReceiveTimeout = SeqismConstant.RECEIVE_TIME_OUT;
    }

    /**
     * Sends the specified {@link SeqismMessage} and waits for a corresponding response.
     * <p>
     * This method first sends the provided message using {@code sendMessage(message)},
     * then waits for and returns the received message using {@code receivedMessage(message)}.
     *
     * @param <R>
     *            the type of the response message payload
     * @param <C>
     *            the type of the command message payload
     * @param message
     *            the message to send and await a response for
     * @return the received {@link SeqismMessage} corresponding to the sent message
     * @throws RuntimeException
     *             if sending or receiving the message fails
     */
    public <R, C> SeqismMessage<R> sendAndReceiveOrThrow(SeqismMessage<C> message) {
        sendMessage(message);
        return receivedMessage(message);
    }

    /**
     * Sends the specified {@link SeqismMessage} as a final message in the processing queue.
     * <p>
     * This method delegates to {@code sendMessage} to perform the actual sending operation.
     *
     * @param <T>
     *            the type of the payload contained in the message
     * @param message
     *            the message to be sent as final
     */
    public <T> void sendFinal(SeqismMessage<T> message) {
        sendMessage(message);
    }

    /**
     * Sends a {@link SeqismMessage} to the appropriate command queue using RabbitMQ.
     * <p>
     * The queue name is determined based on the transaction ID found in the message header.
     * If the message cannot be sent due to an AMQP-related exception, a {@link SeqismException}
     * is thrown with the corresponding error information.
     *
     * @param <T>
     *            the type of the payload contained in the message
     * @param message
     *            the message to be sent to the command queue
     * @throws SeqismException
     *             if an error occurs while sending the message to the queue
     */
    <T> void sendMessage(SeqismMessage<T> message) {
        log.debug("Sending message : [{}]", message);
        String commandQueueName = queueNameHelper.getCommandQueueName(message.getHeader().getTranId());

        try {
            rabbitTemplate.convertAndSend(commandQueueName, message);
        } catch (AmqpException e) {
            throw new SeqismException(ErrorInfo.ERROR_0002_0004, e);
        }
    }

    /**
     * Receives a message from the response queue corresponding to the given message's transaction ID.
     * <p>
     * Waits for a response message from the queue for a specified timeout period. If a message is received,
     * it is returned; otherwise, a timeout error is logged and a {@link SeqismException} is thrown.
     * Handles AMQP exceptions by wrapping them in a {@link SeqismException}.
     *
     * @param <R>
     *            the type of the response message payload
     * @param <C>
     *            the type of the command message payload
     * @param message
     *            the original {@link SeqismMessage} for which a response is expected
     * @return the received {@link SeqismMessage} from the response queue
     * @throws SeqismException
     *             if a timeout occurs or an AMQP error is encountered while receiving the message
     */
    <R, C> SeqismMessage<R> receivedMessage(SeqismMessage<C> message) {
        String responseQueueName = queueNameHelper.getResponseQueueName(message.getHeader().getTranId());
        ParameterizedTypeReference<SeqismMessage<R>> typeRef = new ParameterizedTypeReference<SeqismMessage<R>>() {};

        try {
            SeqismMessage<R> receivedMsg //
                    = rabbitTemplate.receiveAndConvert(responseQueueName, this.messageReceiveTimeout, typeRef);
            if (receivedMsg == null) {
                log.error("Timeout occurred while waiting for response from queue : [{}]", responseQueueName);
                throw new SeqismException(ErrorInfo.ERROR_0002_0003);
            }

            log.debug("Received message : [{}]", receivedMsg);
            return receivedMsg;
        } catch (AmqpException e) {
            throw new SeqismException(ErrorInfo.ERROR_0002_0005, e);
        }
    }
}