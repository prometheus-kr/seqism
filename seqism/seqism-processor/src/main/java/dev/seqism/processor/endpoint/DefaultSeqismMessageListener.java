package dev.seqism.processor.endpoint;

import dev.seqism.common.vo.ErrorInfo;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.processor.helper.ProcessorQueueHelper;
import dev.seqism.processor.SeqismProcessor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Default implementation of {@link SeqismMessageListener} that routes incoming messages
 * to the appropriate {@link SeqismProcessor} based on the business code (bizCode) found in the message header.
 * <p>
 * This listener uses a map of processors, each keyed by its business code, to efficiently dispatch
 * messages to the correct processor. If no processor is found for a given bizCode, the message is marked as failed.
 *
 * <p>
 * Dependencies:
 * <ul>
 * <li>{@link ObjectMapper} for converting message bodies to the required type.</li>
 * <li>{@link ProcessorQueueHelper} for queue operations and sending failure messages.</li>
 * <li>A list of {@link SeqismProcessor} implementations, each handling a specific business code.</li>
 * </ul>
 *
 * <p>
 * Logging is provided via Lombok's {@code @Slf4j} annotation.
 */
@Slf4j
@Component
public class DefaultSeqismMessageListener extends SeqismMessageListener<Object> {
    /**
     * Jackson {@link ObjectMapper} instance used for serializing and deserializing JSON messages.
     */
    private final ObjectMapper mapper;
    /**
     * A map that associates a unique string key with a corresponding {@link SeqismProcessor} instance.
     * This allows for efficient retrieval and management of different processor implementations
     * based on their identifier.
     */
    private final Map<String, SeqismProcessor<?, ?>> processorMap;

    /**
     * Constructs a new {@code DefaultSeqismMessageListener} with the specified {@link ObjectMapper},
     * {@link ProcessorQueueHelper}, and a list of {@link SeqismProcessor} instances.
     *
     * <p>
     * This constructor initializes the message listener by setting up the object mapper for JSON
     * serialization/deserialization, the queue helper for processing message queues, and a map of
     * business code to processor for efficient message handling.
     *
     * @param mapper
     *            the {@link ObjectMapper} used for JSON processing
     * @param queueHelper
     *            the {@link ProcessorQueueHelper} used to manage processing queues
     * @param processors
     *            the list of {@link SeqismProcessor} instances to handle different business codes
     */
    DefaultSeqismMessageListener(ObjectMapper mapper, ProcessorQueueHelper queueHelper,
            List<SeqismProcessor<?, ?>> processors) {
        super(queueHelper);
        this.mapper = mapper;
        this.processorMap = processors.stream().collect(Collectors.toMap(SeqismProcessor::getBizCode, p -> p));
    }

    /**
     * Processes the given {@link SeqismMessage} by determining the appropriate processor
     * based on the message's business code. If a matching processor is found, it delegates
     * the message to the processor for handling. If no processor is found for the business code,
     * logs an error and sends a failure response using the queue helper.
     *
     * @param message
     *            the message to be processed, containing headers and payload
     */
    @Override
    void proc(SeqismMessage<Object> message) {
        String bizCode = message.getHeader().getBizCode();
        SeqismProcessor<?, ?> processor = processorMap.get(bizCode);

        if (processor != null) {
            callProcessor(processor, message);
        } else {
            log.error("No processor found for bizCode : [{}]", bizCode);
            queueHelper.sendFinal(message.toFailure(ErrorInfo.ERROR_0002_0001, bizCode));
        }
    }

    /**
     * Invokes the specified {@link SeqismProcessor} with a message whose body is converted
     * to the processor's expected type using the configured {@code mapper}.
     *
     * @param <R>
     *            the type of the response message payload
     * @param <C>
     *            the type of the command message payload
     * @param processor
     *            the processor to handle the message
     * @param message
     *            the incoming message with a body to be converted and processed
     */
    <R, C> void callProcessor(SeqismProcessor<R, C> processor, SeqismMessage<Object> message) {
        processor.process(message.withBody(mapper.convertValue(message.getBody(), processor.getBodyType())));
    }
}