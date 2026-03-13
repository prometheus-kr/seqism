package io.github.prometheuskr.seqism.processor.endpoint;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

import io.github.prometheuskr.seqism.common.constant.SeqismConstant;
import io.github.prometheuskr.seqism.common.vo.ErrorInfo;
import io.github.prometheuskr.seqism.common.vo.SeqismException;
import io.github.prometheuskr.seqism.common.vo.SeqismMessage;
import io.github.prometheuskr.seqism.processor.helper.ProcessorQueueHelper;

/**
 * Abstract base class for message listeners that process {@link SeqismMessage} instances
 * received from a RabbitMQ queue. This listener handles asynchronous message processing,
 * error handling, and failure message dispatching using a {@link ProcessorQueueHelper}.
 * <p>
 * Subclasses must implement the {@link #proc(SeqismMessage)} method to define custom
 * message processing logic for messages of type {@code T}.
 * <p>
 * Error handling is performed as follows:
 * <ul>
 * <li>If a {@link SeqismException} is thrown during processing, the error is logged and
 * a failure message with the associated error information is sent to the final queue.</li>
 * <li>For any other exceptions, the error is logged and a generic failure message with
 * error code {@link ErrorInfo#ERROR_0002_0002} and the exception message is sent.</li>
 * </ul>
 *
 * @param <T>
 *            the type of the payload contained in the {@link SeqismMessage}
 */
@Slf4j
public abstract class SeqismMessageListener<T> {
    /**
     * Helper instance for managing and interacting with the processor's message queue.
     * Provides utility methods to enqueue, dequeue, and process messages within the system.
     */
    protected final ProcessorQueueHelper queueHelper;

    /**
     * Constructs a new {@code SeqismMessageListener} with the specified {@link ProcessorQueueHelper}.
     *
     * @param queueHelper
     *            the helper used for processing message queues
     */
    SeqismMessageListener(ProcessorQueueHelper queueHelper) {
        this.queueHelper = queueHelper;
    }

    /**
     * Handles incoming messages from the configured RabbitMQ queue asynchronously.
     * Processes the received {@link SeqismMessage} using the {@code proc} method.
     * If a {@link SeqismException} occurs during processing, logs the error and sends a failure message
     * with the associated error information to the final queue.
     * For any other exceptions, logs the error and sends a generic failure message with error code
     * {@link ErrorInfo#ERROR_0002_0002} and the exception message.
     *
     * @param message
     *            the message received from the queue to be processed
     */
    @RabbitListener(queues = "${seqism.queue.static.name:" + SeqismConstant.SEQISM_STATIC_QUEUE + "}")
    public void handleMessage(SeqismMessage<T> message) {
        CompletableFuture.runAsync(() -> {
            try {
                this.proc(message);
            } catch (SeqismException e) {
                log.error("An exception occurred while processing", e);
                queueHelper.sendFinal((message.toFailure(e.getErrorInfo())));
            } catch (Exception e) {
                log.error("An exception occurred while processing", e);
                queueHelper.sendFinal((message.toFailure(ErrorInfo.ERROR_0002_0002, e.getMessage())));
            }
        });
    }

    /**
     * Handles incoming messages from the one-time RabbitMQ queue synchronously (RPC-style).
     * Processes the message in a single round-trip and returns the response directly.
     * Spring AMQP automatically sends the returned value back to the caller's reply queue.
     *
     * @param message
     *            the message received from the one-time queue
     * @return the processed response message
     */
    @RabbitListener(queues = "${seqism.queue.onetime.name:" + SeqismConstant.SEQISM_ONETIME_QUEUE + "}")
    public SeqismMessage<?> handleOnetimeMessage(SeqismMessage<T> message) {
        try {
            return this.procOnetime(message);
        } catch (SeqismException e) {
            log.error("An exception occurred while processing onetime message", e);
            return message.toFailure(e.getErrorInfo());
        } catch (Exception e) {
            log.error("An exception occurred while processing onetime message", e);
            return message.toFailure(ErrorInfo.ERROR_0002_0002, e.getMessage());
        }
    }

    /**
     * Processes the given {@link SeqismMessage}.
     * <p>
     * This method should be implemented by subclasses to define how a {@code SeqismMessage}
     * of type {@code T} is handled.
     *
     * @param seqismMessage
     *            the message to process
     */
    abstract void proc(SeqismMessage<T> seqismMessage);

    /**
     * Processes a one-time (RPC-style) {@link SeqismMessage} and returns the response directly.
     * <p>
     * This method should be implemented by subclasses to define how a one-time
     * {@code SeqismMessage} of type {@code T} is handled.
     *
     * @param seqismMessage
     *            the message to process
     * @return the response message to return to the caller
     */
    abstract SeqismMessage<?> procOnetime(SeqismMessage<T> seqismMessage);
}