package dev.seqism.common.vo;

/**
 * Represents the status of a Seqism message.
 * <ul>
 * <li>{@link #IN_PROGRESS} - The message is currently being processed.</li>
 * <li>{@link #SUCCESS} - The message has been processed successfully.</li>
 * <li>{@link #FAILURE} - The message processing has failed.</li>
 * </ul>
 *
 * @author Prometheus-kr
 * @since 0.0.1
 */
public enum SeqismMessageStatus {
    /**
     * The message is currently being processed.
     */
    IN_PROGRESS,
    /**
     * The message has been processed successfully.
     */
    SUCCESS,
    /**
     * The message processing has failed.
     */
    FAILURE
}