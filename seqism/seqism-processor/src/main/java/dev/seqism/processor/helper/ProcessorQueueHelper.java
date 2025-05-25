package dev.seqism.processor.helper;

import dev.seqism.common.constant.SeqismConstant;
import dev.seqism.common.helper.QueueNameHelper;
import dev.seqism.common.vo.ErrorInfo;
import dev.seqism.common.vo.SeqismException;
import dev.seqism.common.vo.SeqismMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

/**
 * Helper class for managing message sending and receiving operations with RabbitMQ queues
 * in the processor module. Provides convenient methods for sending messages to command queues,
 * receiving responses from response queues, and handling message exchange patterns such as
 * request-response and final message dispatch.
 * <p>
 * Utilizes {@link RabbitTemplate} for interacting with RabbitMQ and {@link QueueNameHelper}
 * for dynamic queue name resolution based on transaction IDs.
 * <p>
 * Main responsibilities include:
 * <ul>
 * <li>Sending messages to command queues and handling AMQP exceptions.</li>
 * <li>Receiving messages from response queues with timeout and error handling.</li>
 * <li>Supporting request-response patterns via {@code sendAndReceiveOrThrow}.</li>
 * <li>Sending final messages to queues.</li>
 * </ul>
 * <p>
 * All exceptions related to message operations are wrapped in {@link SeqismException}
 * with appropriate error information.
 */
@Slf4j
@Component
@AllArgsConstructor
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
     * Sends the specified {@link SeqismMessage} and waits for a corresponding response.
     * <p>
     * This method first sends the provided message using {@code sendMessage(message)},
     * then waits for and returns the received message using {@code receivedMessage(message)}.
     *
     * @param <T>
     *            the type of the payload contained in the message
     * @param message
     *            the message to send and await a response for
     * @return the received {@link SeqismMessage} corresponding to the sent message
     * @throws RuntimeException
     *             if sending or receiving the message fails
     */
    public <T> SeqismMessage<T> sendAndReceiveOrThrow(SeqismMessage<T> message) {
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
     * @param message
     *            the message to be sent to the command queue
     * @param <T>
     *            the type of the payload contained in the message
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
     * This method attempts to receive and convert a message from the response queue using the provided
     * {@link SeqismMessage} as a reference for the expected message type. If a message is not received
     * within the configured timeout, a {@link SeqismException} is thrown indicating a timeout error.
     * Any {@link AmqpException} encountered during the receive operation is also wrapped and rethrown
     * as a {@link SeqismException}.
     *
     * @param message
     *            the original {@link SeqismMessage} containing the transaction ID used to determine the response queue
     * @param <T>
     *            the type of the payload contained in the {@link SeqismMessage}
     * @return the received {@link SeqismMessage} from the response queue
     * @throws SeqismException
     *             if a timeout occurs or an AMQP error is encountered while receiving the message
     */
    <T> SeqismMessage<T> receivedMessage(SeqismMessage<T> message) {
        String responseQueueName = queueNameHelper.getResponseQueueName(message.getHeader().getTranId());
        ParameterizedTypeReference<SeqismMessage<T>> typeRef = new ParameterizedTypeReference<SeqismMessage<T>>() {
        };

        try {
            SeqismMessage<T> receivedMsg //
                    = rabbitTemplate.receiveAndConvert(responseQueueName, SeqismConstant.RECEIVE_TIME_OUT, typeRef);
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