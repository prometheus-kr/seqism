package io.github.prometheuskr.seqism.common.helper;

/**
 * Helper class for constructing queue names used in messaging or task processing systems.
 * <p>
 * This class encapsulates the logic for generating static, command, and response queue names,
 * typically by combining a base prefix with a transaction identifier.
 * <ul>
 * <li><b>staticQueueName</b>: The fixed name of a static queue.</li>
 * <li><b>commandQueuePrefix</b>: The base prefix for command queues, to which a transaction ID is appended.</li>
 * <li><b>responseQueuePrefix</b>: The base prefix for response queues, to which a transaction ID is appended.</li>
 * </ul>
 * <p>
 * Example usage:
 * 
 * <pre>
 * QueueNameHelper helper = new QueueNameHelper("staticQueue", "cmdQueue-", "respQueue-");
 * String commandQueue = helper.getCommandQueueName("123"); // "cmdQueue-123"
 * String responseQueue = helper.getResponseQueueName("123"); // "respQueue-123"
 * </pre>
 */
public class QueueNameHelper {
    /**
     * The name of the static queue associated with this instance.
     * This value is assigned at construction time and does not change.
     */
    private final String staticQueueName;
    /**
     * Prefix used for naming command queues.
     * This value is typically set during initialization and is used to construct
     * the full names of command queues within the application.
     */
    private final String commandQueuePrefix;
    /**
     * Prefix used for naming response queues.
     * This value is typically prepended to queue names to distinguish response queues
     * from other types of queues within the system.
     */
    private final String responseQueuePrefix;

    /**
     * Constructs a new {@code QueueNameHelper} with the specified queue names.
     *
     * @param staticQueueName
     *            the name of the static queue
     * @param commandQueuePrefix
     *            the name of the command queue
     * @param responseQueuePrefix
     *            the name of the response queue
     */
    public QueueNameHelper(String staticQueueName, String commandQueuePrefix, String responseQueuePrefix) {
        this.staticQueueName = staticQueueName;
        this.commandQueuePrefix = commandQueuePrefix;
        this.responseQueuePrefix = responseQueuePrefix;
    }

    /**
     * Retrieves the static queue name.
     *
     * @return the name of the static queue
     */
    public String getStaticQueueName() {
        return staticQueueName;
    }

    /**
     * Generates the command queue name by appending the given transaction ID to the command queue prefix.
     *
     * @param tranId
     *            the transaction ID to be appended to the command queue prefix
     * @return the full command queue name for the specified transaction
     */
    public String getCommandQueueName(String tranId) {
        return commandQueuePrefix + tranId;
    }

    /**
     * Generates the response queue name by appending the given transaction ID to the response queue prefix.
     *
     * @param tranId
     *            the transaction ID to be appended to the response queue prefix
     * @return the full response queue name for the specified transaction ID
     */
    public String getResponseQueueName(String tranId) {
        return responseQueuePrefix + tranId;
    }
}