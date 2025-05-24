package dev.seqism.processor.endpoint;

import dev.seqism.common.constant.SeqismConstant;
import dev.seqism.common.vo.ErrorInfo;
import dev.seqism.common.vo.SeqismException;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.processor.helper.ProcessorQueueHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

/**
 * Abstract base class for message listeners that process {@link SeqismMessage} objects.
 * <p>
 * Subclasses must implement the {@link #proc(SeqismMessage)} method to define custom processing logic.
 * This class handles asynchronous execution of message processing and error handling.
 * If an exception occurs during processing, it logs the error and sends a failure message using
 * {@link ProcessorQueueHelper}.
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
     * Handles incoming messages from the RabbitMQ queue specified by {@link SeqismConstant#SEQISM_STATIC_QUEUE}.
     * Processes the message asynchronously using {@link CompletableFuture#runAsync(Runnable)}.
     * <p>
     * If a {@link SeqismException} occurs during processing, logs the error and sends a failure response
     * with the specific error information. For any other exceptions, logs the error and sends a generic
     * failure response with a standard error code and the exception message.
     *
     * @param message
     *            the {@link SeqismMessage} to be processed
     */
    @RabbitListener(queues = SeqismConstant.SEQISM_STATIC_QUEUE)
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
     * Processes the given {@link SeqismMessage}.
     *
     * <p>
     * This method should be implemented by subclasses to define how a {@code SeqismMessage}
     * of type {@code T} is handled.
     *
     * @param seqismMessage
     *            the message to process
     */
    abstract void proc(SeqismMessage<T> seqismMessage);
}