package dev.seqism.common.vo;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents the header information for a Seqism message, encapsulating business code,
 * transaction ID, message status, and error details.
 * <p>
 * This class is immutable and provides utility methods to create new instances
 * with updated status or error information.
 *
 * <p>
 * Typical usage:
 * 
 * <pre>
 * SeqismMessageHeader header = new SeqismMessageHeader("BIZ001", "TX123", SeqismMessageStatus.IN_PROGRESS, null);
 * SeqismMessageHeader successHeader = header.toSuccess();
 * </pre>
 */
@Data
@AllArgsConstructor
public class SeqismMessageHeader implements Serializable {
    /**
     * The business code associated with the message, used to identify the specific business context or operation.
     */
    private final String bizCode;
    /**
     * Unique identifier for the transaction associated with this message.
     */
    private final String tranId;
    /**
     * The status of the message, represented by a {@link SeqismMessageStatus} enum.
     * Indicates the current state or outcome associated with this message header.
     */
    private final SeqismMessageStatus status;
    /**
     * Contains information about an error that occurred, if any.
     * This field is {@code null} if no error is present.
     */
    private final ErrorInfo error;

    /**
     * Default constructor for {@code SeqismMessageHeader}.
     * <p>
     * Initializes a new instance of {@code SeqismMessageHeader} with default values:
     * {@code null} for unspecified fields and {@code SeqismMessageStatus.FAILURE} as the default status.
     */
    public SeqismMessageHeader() {
        this(null, null, SeqismMessageStatus.FAILURE, null);
    }

    /**
     * Creates a new {@code SeqismMessageHeader} instance representing the "in progress" state,
     * using the current transaction ID.
     *
     * @return a new {@code SeqismMessageHeader} with the status set to "in progress"
     */
    SeqismMessageHeader toInProgress() {
        return toInProgress(tranId);
    }

    /**
     * Creates a new {@link SeqismMessageHeader} instance with the specified transaction ID
     * and sets the message status to {@code IN_PROGRESS}, preserving the current business code.
     *
     * @param tranId
     *            the transaction ID to associate with the new message header
     * @return a new {@link SeqismMessageHeader} with the updated transaction ID and status set to {@code IN_PROGRESS}
     */
    SeqismMessageHeader toInProgress(String tranId) {
        return of(bizCode, tranId, SeqismMessageStatus.IN_PROGRESS);
    }

    /**
     * Returns a new {@code SeqismMessageHeader} instance with the same business code and transaction ID,
     * but with the message status set to {@code SeqismMessageStatus.SUCCESS}.
     *
     * @return a {@code SeqismMessageHeader} representing a successful message status
     */
    SeqismMessageHeader toSuccess() {
        return of(bizCode, tranId, SeqismMessageStatus.SUCCESS);
    }

    /**
     * Creates a new {@code SeqismMessageHeader} instance representing a failure state,
     * using the current business code and transaction ID, and the provided error information.
     *
     * @param errorInfo
     *            the error information to associate with the failure message header
     * @return a new {@code SeqismMessageHeader} with status set to {@code FAILURE} and the specified error information
     */
    SeqismMessageHeader toFailure(ErrorInfo errorInfo) {
        return of(bizCode, tranId, SeqismMessageStatus.FAILURE, errorInfo);
    }

    /**
     * Creates a new {@code SeqismMessageHeader} instance with the specified business code, transaction ID, and message
     * status.
     * This is a convenience method that delegates to {@link #of(String, String, SeqismMessageStatus, Object)} with a
     * {@code null} value for the additional parameter.
     *
     * @param bizCode
     *            the business code associated with the message
     * @param tranId
     *            the transaction ID for the message
     * @param status
     *            the status of the message
     * @return a new {@code SeqismMessageHeader} instance
     */
    static SeqismMessageHeader of(String bizCode, String tranId, SeqismMessageStatus status) {
        return of(bizCode, tranId, status, null);
    }

    /**
     * Creates a new instance of {@link SeqismMessageHeader} with the specified business code, transaction ID,
     * message status, and error information.
     *
     * @param bizCode
     *            the business code associated with the message
     * @param tranId
     *            the transaction ID for the message
     * @param status
     *            the status of the message
     * @param errorInfo
     *            the error information, if any, associated with the message
     * @return a new {@link SeqismMessageHeader} instance containing the provided details
     */
    static SeqismMessageHeader of(String bizCode, String tranId, SeqismMessageStatus status, ErrorInfo errorInfo) {
        return new SeqismMessageHeader(bizCode, tranId, status, errorInfo);
    }
}