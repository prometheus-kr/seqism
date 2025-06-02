package io.github.prometheuskr.seqism.processor;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.prometheuskr.seqism.common.vo.SeqismMessage;
import io.github.prometheuskr.seqism.processor.helper.ProcessorQueueHelper;

/**
 * Abstract base class for processing Seqism messages with a specific body type.
 * <p>
 * This processor uses an {@link ObjectMapper} for converting message bodies and a
 * {@link ProcessorQueueHelper} for handling message queue operations. Subclasses must
 * implement business-specific logic by providing the business code, body type, and
 * message processing implementation.
 *
 * @param <R>
 *            the type of the response message payload
 * @param <C>
 *            the type of the command message payload
 */
public abstract class SeqismProcessor<R, C> {
    /**
     * The {@code ObjectMapper} instance used for serializing and deserializing JSON objects.
     * This is typically configured to handle the application's specific data formats.
     */
    protected final ObjectMapper mapper;
    /**
     * Helper instance for managing and interacting with the processor's internal queue.
     * Provides utility methods to enqueue, dequeue, and inspect queued items during processing.
     */
    protected final ProcessorQueueHelper queueHelper;

    /**
     * Constructs a new {@code SeqismProcessor} with the specified {@link ObjectMapper} and
     * {@link ProcessorQueueHelper}.
     *
     * @param mapper
     *            the {@link ObjectMapper} used for JSON serialization and deserialization
     * @param queueHelper
     *            the {@link ProcessorQueueHelper} used to manage processing queues
     */
    public SeqismProcessor(ObjectMapper mapper, ProcessorQueueHelper queueHelper) {
        this.mapper = mapper;
        this.queueHelper = queueHelper;
    }

    /**
     * Sends the specified {@link SeqismMessage} and waits for a response, throwing an exception if an error occurs.
     * <p>
     * This method delegates the send-and-receive operation to {@code queueHelper}, then converts the response body
     * to the expected type using the configured {@code mapper}. The returned message contains the converted body.
     *
     * @param message
     *            the message to send
     * @return the response message with the body converted to the expected type
     * @throws RuntimeException
     *             if the send or receive operation fails
     */
    protected SeqismMessage<R> sendAndReceiveOrThrow(SeqismMessage<C> message) {
        SeqismMessage<R> response = queueHelper.sendAndReceiveOrThrow(message);
        R respBody = mapper.convertValue(response.getBody(), getBodyType());
        return response.withBody(respBody);
    }

    /**
     * Sends the specified {@link SeqismMessage} as a final message using the queue helper.
     * This method is typically used to indicate that no further processing is required for the message.
     *
     * @param message
     *            the message to be sent as final
     */
    protected void sendFinal(SeqismMessage<C> message) {
        queueHelper.sendFinal(message);
    }

    /**
     * Retrieves the business code associated with the current context or entity.
     *
     * @return a {@code String} representing the business code.
     */
    public abstract String getBizCode();

    /**
     * Returns the {@link Class} object representing the type parameter {@code T}
     * that defines the body type handled by this processor.
     *
     * @return the {@link Class} of the body type {@code T}
     */
    public abstract Class<R> getBodyType();

    /**
     * Processes the given {@link SeqismMessage}.
     * <p>
     * This method should be implemented to define how a {@code SeqismMessage} of type {@code T}
     * is handled by the processor.
     *
     * @param message
     *            the message to process; must not be {@code null}
     */
    public abstract void process(SeqismMessage<R> message);
}