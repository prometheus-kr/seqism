package dev.seqism.common.vo;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a generic message structure with a header and a body, used for communication
 * within the Seqism system. The message encapsulates a {@link SeqismMessageHeader} and a
 * body of generic type {@code T}. Provides utility methods to create new messages with
 * updated status (success, in-progress, failure) and to transform the message body.
 *
 * @param <T>
 *            the type of the message body
 *
 *            <p>
 *            Features:
 *            <ul>
 *            <li>Immutable header and body fields.</li>
 *            <li>Factory methods for status transitions (success, in-progress, failure).</li>
 *            <li>Ability to create a new message with a different body.</li>
 *            </ul>
 *
 *            <p>
 *            Example usage:
 * 
 *            <pre>
 *            SeqismMessage&lt;String&gt; msg = new SeqismMessage&lt;&gt;(header, "payload");
 *            SeqismMessage&lt;String&gt; successMsg = msg.toSuccess();
 *            SeqismMessage&lt;Integer&gt; newBodyMsg = msg.withBody(123);
 *            </pre>
 *
 * @see SeqismMessageHeader
 * @see ErrorInfo
 */
@Data
@AllArgsConstructor
public class SeqismMessage<T> implements Serializable {
    /**
     * The header information associated with this message.
     * Contains metadata such as message type, timestamp, and sender details.
     */
    private final SeqismMessageHeader header;
    /**
     * The payload of the message, containing the main data of type {@code T}.
     */
    private final T body;

    /**
     * Default constructor for {@code SeqismMessage}.
     * Initializes a new instance of the class with default values by delegating to the parameterized constructor with
     * {@code null} arguments.
     */
    public SeqismMessage() {
        this(null, null);
    }

    /**
     * Converts the current message to a success state by updating the header to indicate success,
     * while retaining the existing body.
     *
     * @return a new {@code SeqismMessage} instance with a success header and the same body.
     */
    public SeqismMessage<T> toSuccess() {
        return of(header.toSuccess(), body);
    }

    /**
     * Returns a new {@code SeqismMessage} instance with the header marked as "in progress"
     * while retaining the current body.
     *
     * @return a new {@code SeqismMessage} with the updated header state and the same body
     */
    public SeqismMessage<T> toInProgress() {
        return of(header.toInProgress(), body);
    }

    /**
     * Returns a new {@code SeqismMessage} instance with the header updated to indicate
     * an "in progress" status for the specified transaction ID, while retaining the original body.
     *
     * @param tranId
     *            the transaction ID to set in the header as "in progress"
     * @return a new {@code SeqismMessage} instance with the updated header and the same body
     */
    public SeqismMessage<T> toInProgress(String tranId) {
        return of(header.toInProgress(tranId), body);
    }

    /**
     * Converts the current {@code SeqismMessage} instance to a failure state using the provided {@link ErrorInfo}.
     * <p>
     * This method creates a new {@code SeqismMessage} with a failure header based on the given error information,
     * and sets the message body to {@code null}.
     *
     * @param errorInfo
     *            the error information to be used for the failure header
     * @return a new {@code SeqismMessage} instance representing a failure, with the specified error information
     */
    public <U> SeqismMessage<U> toFailure(ErrorInfo errorInfo) {
        return of(header.toFailure(errorInfo), null);
    }

    /**
     * Converts the current {@code SeqismMessage} to a failure state using the provided {@code ErrorInfo}
     * and an additional error message.
     *
     * @param errorInfo
     *            the {@code ErrorInfo} object containing error details
     * @param message
     *            the additional error message to be added
     * @return a new {@code SeqismMessage} instance representing a failure, with the updated error information and no
     *         payload
     */
    public <U> SeqismMessage<U> toFailure(ErrorInfo errorInfo, String message) {
        return of(header.toFailure(errorInfo.addMessage(message)), null);
    }

    /**
     * Returns a new {@code SeqismMessage} instance with the specified body and the existing header.
     *
     * @param <N>
     *            the type of the new body
     * @param body
     *            the new body to set in the message
     * @return a new {@code SeqismMessage} instance with the same header and the provided body
     */
    public <N> SeqismMessage<N> withBody(N body) {
        return of(header, body);
    }

    /**
     * Creates a new {@link SeqismMessage} instance with the specified header and body.
     *
     * @param <T>
     *            the type of the message body
     * @param header
     *            the message header
     * @param body
     *            the message body
     * @return a new {@code SeqismMessage} containing the given header and body
     */
    static <U> SeqismMessage<U> of(SeqismMessageHeader header, U body) {
        return new SeqismMessage<U>(header, body);
    }
}