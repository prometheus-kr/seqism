package io.github.prometheuskr.seqism.gateway.helper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import io.github.prometheuskr.seqism.common.constant.SeqismConstant;
import io.github.prometheuskr.seqism.common.helper.QueueNameHelper;
import io.github.prometheuskr.seqism.common.vo.ErrorInfo;
import io.github.prometheuskr.seqism.common.vo.SeqismException;
import io.github.prometheuskr.seqism.common.vo.SeqismMessage;
import io.github.prometheuskr.seqism.common.vo.SeqismMessageStatus;

/**
 * Gateway-side helper for RabbitMQ queue management and message exchange.
 * <p>
 * Supports two messaging patterns:
 * <ul>
 * <li><b>Multi-step (init/next)</b> — Creates temporary command/response queues per transaction.
 * Use {@link #sendAndReceiveInit(SeqismMessage)} to start and {@link #sendAndReceiveNext(SeqismMessage)}
 * to continue. Queues are automatically deleted when the transaction completes.</li>
 * <li><b>One-time (RPC)</b> — Synchronous single-transaction exchange via
 * {@link #sendAndReceiveOnetime(SeqismMessage)}. Uses Spring AMQP's built-in RPC mechanism
 * without creating temporary queues.</li>
 * </ul>
 * <p>
 * Configurable application properties:
 * <ul>
 * <li>{@code seqism.queue.delete.timeout} — Queue expiration time in ms
 * (default: {@link SeqismConstant#QUEUE_DELETE_TIME})</li>
 * <li>{@code seqism.queue.receive.timeout} — Receive/reply timeout in ms, configured globally on
 * {@link RabbitTemplate} (default: {@link SeqismConstant#RECEIVE_TIME_OUT})</li>
 * </ul>
 */
@Slf4j
@Component
public class GateWayQueueHelper {
    /** {@link RabbitAdmin} for declaring and deleting AMQP queues. */
    private final RabbitAdmin rabbitAdmin;
    /** {@link RabbitTemplate} for sending and receiving messages. */
    private final RabbitTemplate rabbitTemplate;
    /** Helper for generating queue names based on prefixes and transaction IDs. */
    private final QueueNameHelper queueNameHelper;
    /** Queue expiration time (ms). Defaults to {@link SeqismConstant#QUEUE_DELETE_TIME}. */
    private final long queueDeleteTimeout;

    /**
     * Constructs a {@code GateWayQueueHelper} with the required dependencies and configuration.
     *
     * @param rabbitAdmin
     *            AMQP resource manager
     * @param rabbitTemplate
     *            message sender/receiver (timeout is pre-configured)
     * @param queueNameHelper
     *            queue name generator
     * @param queueDeleteTimeout
     *            queue TTL in ms ({@code seqism.queue.delete.timeout})
     */
    public GateWayQueueHelper(
            RabbitAdmin rabbitAdmin,
            RabbitTemplate rabbitTemplate,
            QueueNameHelper queueNameHelper,
            @Value("${seqism.queue.delete.timeout:" + SeqismConstant.QUEUE_DELETE_TIME + "}") long queueDeleteTimeout) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
        this.queueNameHelper = queueNameHelper;
        this.queueDeleteTimeout = queueDeleteTimeout;
    }

    /**
     * Starts a multi-step transaction: creates command/response queues, sends the message
     * to the static queue, and waits for the first response.
     *
     * @param <R>
     *            request payload type
     * @param <C>
     *            response payload type
     * @param message
     *            the initial message to send
     * @return the first response from the processor
     */

    public <R, C> SeqismMessage<C> sendAndReceiveInit(SeqismMessage<R> message) {
        log.debug("Sending message : [{}]", message);

        createQueues(message);

        send(queueNameHelper.getStaticQueueName(), message);
        return receive(message);
    }

    /**
     * Continues a multi-step transaction: sends the message to the response queue
     * identified by the transaction ID, and waits for the next response.
     *
     * @param <R>
     *            request payload type
     * @param <C>
     *            response payload type
     * @param message
     *            the message to send
     * @return the next response from the processor
     */
    public <R, C> SeqismMessage<C> sendAndReceiveNext(SeqismMessage<R> message) {
        log.debug("Sending message : [{}]", message);
        String tranId = message.getHeader().getTranId();

        send(queueNameHelper.getResponseQueueName(tranId), message);
        return receive(message);
    }

    /**
     * Performs a synchronous RPC-style one-time exchange: sends the message to the
     * onetime queue and blocks until the processor returns a response.
     * No temporary queues are created.
     *
     * @param <R>
     *            request payload type
     * @param <C>
     *            response payload type
     * @param message
     *            the message to send
     * @return the response from the processor
     * @throws SeqismException
     *             if an AMQP error occurs
     */
    public <R, C> SeqismMessage<C> sendAndReceiveOnetime(SeqismMessage<R> message) {
        log.debug("Sending onetime message : [{}]", message);

        String onetimeQueueName = queueNameHelper.getOnetimeQueueName();
        ParameterizedTypeReference<SeqismMessage<C>> typeRef = new ParameterizedTypeReference<SeqismMessage<C>>() {};

        try {
            SeqismMessage<C> response = rabbitTemplate.convertSendAndReceiveAsType(onetimeQueueName, message, typeRef);
            log.debug("Received onetime response : [{}]", response);
            return response;
        } catch (AmqpException e) {
            throw new SeqismException(ErrorInfo.ERROR_0001_0004, e);
        }
    }

    /**
     * Creates command and response queues for the given transaction.
     *
     * @param message
     *            the message whose transaction ID determines the queue names
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
     * Declares a durable queue with {@code x-expires} set to {@link #queueDeleteTimeout}.
     *
     * @param queueName
     *            the queue name
     */
    void declareQueue(String queueName) {
        Queue queue = QueueBuilder.durable(queueName)
                .withArgument("x-expires", this.queueDeleteTimeout)
                .build();
        rabbitAdmin.declareQueue(queue);
    }

    /**
     * Sends a message to the specified queue after verifying the queue exists.
     *
     * @param <T>
     *            payload type
     * @param queueName
     *            the target queue name
     * @param message
     *            the message to send
     * @throws SeqismException
     *             if the queue does not exist ({@link ErrorInfo#ERROR_0001_0003})
     *             or an AMQP error occurs ({@link ErrorInfo#ERROR_0001_0004})
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
     * Receives the next message from the command queue for the given transaction.
     * If the received message is {@code null} or its status is not {@link SeqismMessageStatus#IN_PROGRESS},
     * both command and response queues are deleted.
     *
     * @param <R>
     *            request payload type
     * @param <C>
     *            response payload type
     * @param message
     *            the reference message containing the transaction ID
     * @return the received message, or {@code null} if none was available
     * @throws SeqismException
     *             if an AMQP error occurs ({@link ErrorInfo#ERROR_0001_0005})
     */
    <R, C> SeqismMessage<C> receive(SeqismMessage<R> message) {
        String tranId = message.getHeader().getTranId();

        String commandQueue = queueNameHelper.getCommandQueueName(tranId);
        String responseQueue = queueNameHelper.getResponseQueueName(tranId);

        ParameterizedTypeReference<SeqismMessage<C>> typeRef = new ParameterizedTypeReference<SeqismMessage<C>>() {};

        try {
            SeqismMessage<C> receivedMsg = rabbitTemplate.receiveAndConvert(commandQueue, typeRef);
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