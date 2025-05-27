package dev.seqism.common.constant;

/**
 * Defines constant values used throughout the Seqism application.
 * <p>
 * This class contains configuration values such as timeouts and queue name
 * prefixes
 * for consistent usage across the codebase.
 * <ul>
 * <li>{@link #RECEIVE_TIME_OUT} - Timeout in milliseconds for receiving operations.</li>
 * <li>{@link #QUEUE_DELETE_TIME} - Time in milliseconds after which a queue should be deleted.</li>
 * <li>{@link #SEQISM_STATIC_QUEUE} - Name of the static queue used by Seqism.</li>
 * <li>{@link #COMMAND_QUEUE_PREFIX} - Prefix for command queue names.</li>
 * <li>{@link #RESPONSE_QUEUE_PREFIX} - Prefix for response queue names.</li>
 * </ul>
 */
public class SeqismConstant {
    /**
     * The timeout duration (in milliseconds) for receiving data or responses.
     * A value of 5000 indicates a 5-second timeout period.
     */
    public static final int RECEIVE_TIME_OUT = 5000;
    /**
     * The time interval in milliseconds after which a queue is deleted.
     * Default value is 60000 ms (60 seconds).
     */
    public static final int QUEUE_DELETE_TIME = 60000;
    /**
     * The name of the static queue used in the Seqism application.
     * This constant is typically used for referencing the static queue in messaging
     * or task processing systems.
     */
    public static final String SEQISM_STATIC_QUEUE = "seqism-static-queue";
    /**
     * Prefix used for naming command queues in the Seqism system.
     * This constant is typically appended with a specific identifier to form the
     * full queue name.
     */
    public static final String COMMAND_QUEUE_PREFIX = "seqism-command-queue.";
    /**
     * Prefix used for naming response queues in the Seqism system.
     * This constant helps standardize and identify all response queue names
     * by prepending this prefix to the specific queue identifier.
     */
    public static final String RESPONSE_QUEUE_PREFIX = "seqism-response-queue.";
}
