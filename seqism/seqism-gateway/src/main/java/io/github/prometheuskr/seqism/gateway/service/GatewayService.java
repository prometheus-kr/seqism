package io.github.prometheuskr.seqism.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import io.github.prometheuskr.seqism.common.vo.ErrorInfo;
import io.github.prometheuskr.seqism.common.vo.SeqismException;
import io.github.prometheuskr.seqism.common.vo.SeqismMessage;
import io.github.prometheuskr.seqism.gateway.helper.GateWayQueueHelper;

import java.util.UUID;
import java.util.function.Function;

/**
 * GatewayService provides methods to initialize and progress Seqism message flows
 * by interacting with the GateWayQueueHelper. It handles the sending and receiving
 * of messages, manages transaction IDs, and ensures proper error handling and logging.
 * <p>
 * Main responsibilities:
 * <ul>
 * <li>Initialize a new Seqism message flow with a unique transaction ID.</li>
 * <li>Progress an existing Seqism message flow to the next step.</li>
 * <li>Handle exceptions and convert them into standardized error responses.</li>
 * </ul>
 * <p>
 * Usage:
 * 
 * <pre>
 *     GatewayService service = ...;
 *     SeqismMessage&lt;T&gt; response = service.initSeqism(message);
 * </pre>
 */
@Slf4j
@Service
public class GatewayService {
    /**
     * Helper class responsible for managing gateway queue operations.
     * Used to interact with and manipulate the gateway's request queue.
     */
    private final GateWayQueueHelper queueHelper;

    /**
     * Constructs a new {@code GatewayService} with the specified {@code GateWayQueueHelper}.
     *
     * @param queueHelper
     *            the helper used for managing gateway queues
     */
    public GatewayService(GateWayQueueHelper queueHelper) {
        this.queueHelper = queueHelper;
    }

    /**
     * Initializes a Seqism process by marking the provided message as "in progress" with a generated transaction ID,
     * then sends the message to the appropriate queue and waits for a response.
     *
     * @param <R>
     *            the type of the response message payload
     * @param <C>
     *            the type of the command message payload
     * @param message
     *            the message to initialize and send
     * @return the response message received after initialization
     */
    public <R, C> SeqismMessage<C> initSeqism(SeqismMessage<R> message) {
        return sendAndReceive(message.toInProgress(generateTranId()), queueHelper::sendAndReceiveInit);
    }

    /**
     * Processes the given {@link SeqismMessage} by marking it as in-progress and sending it to the next queue.
     * Utilizes the {@code queueHelper} to send the message and receive the next response.
     *
     * @param <R>
     *            the type of the response message payload
     * @param <C>
     *            the type of the command message payload
     * @param message
     *            the message to be processed and forwarded to the next queue
     * @return the response message received after processing the input message
     */
    public <R, C> SeqismMessage<C> nextSeqism(SeqismMessage<R> message) {
        return sendAndReceive(message.toInProgress(), queueHelper::sendAndReceiveNext);
    }

    /**
     * Generates a unique transaction ID using a randomly generated UUID.
     *
     * @return a string representation of a randomly generated UUID to be used as a transaction ID
     */
    String generateTranId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Sends a {@link SeqismMessage} using the provided sender function and waits for a response.
     * <p>
     * If the sender function returns {@code null}, a failure message is returned with error info
     * {@link ErrorInfo#ERROR_0001_0002}.
     * If a {@link SeqismException} is thrown, a failure message is returned with the exception's error info.
     * For any other exceptions, a failure message is returned with error info {@link ErrorInfo#ERROR_0001_0001} and the
     * exception message.
     * All errors are logged.
     *
     * @param <R>
     *            the type of the response message payload
     * @param <C>
     *            the type of the command message payload
     * @param message
     *            the message to send
     * @param sender
     *            the function that sends the message and returns a response
     * @return the response message, or a failure message if an error occurs
     */
    <R, C> SeqismMessage<C> sendAndReceive(SeqismMessage<R> message,
            Function<SeqismMessage<R>, SeqismMessage<C>> sender) {
        try {
            SeqismMessage<C> response = sender.apply(message);
            return response != null ? response : message.toFailure(ErrorInfo.ERROR_0001_0002);
        } catch (SeqismException e) {
            log.error("Error in GatewayService", e);
            return message.toFailure(e.getErrorInfo());
        } catch (Exception e) {
            log.error("Error in GatewayService", e);
            return message.toFailure(ErrorInfo.ERROR_0001_0001, e.getMessage());
        }
    }
}