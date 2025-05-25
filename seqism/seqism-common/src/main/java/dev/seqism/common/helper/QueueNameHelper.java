package dev.seqism.common.helper;

import lombok.AllArgsConstructor;

/**
 * Helper class for generating queue names used in messaging systems.
 * <p>
 * This class provides methods to retrieve static, command, and response queue names,
 * optionally appending a transaction ID to the command and response queue names.
 */
@AllArgsConstructor
public class QueueNameHelper {
    /**
     * The name of the static queue associated with this instance.
     * This value is assigned at construction time and does not change.
     */
    private final String staticQueueName;
    /**
     * The name of the command queue associated with this instance.
     * This value is typically used to identify and interact with a specific command queue
     * in messaging or task processing systems.
     */
    private final String commandQueueName;
    /**
     * The name of the response queue associated with this instance.
     * Used to identify the queue where responses should be sent or received.
     */
    private final String responseQueueName;

    /**
     * Retrieves the static queue name.
     *
     * @return the name of the static queue
     */
    public String getStaticQueueName() {
        return staticQueueName;
    }

    /**
     * Generates the command queue name by appending the given transaction ID to the base command queue name.
     *
     * @param tranId
     *            the transaction ID to append to the command queue name
     * @return the full command queue name including the transaction ID
     */
    public String getCommandQueueName(String tranId) {
        return commandQueueName + tranId;
    }

    /**
     * Generates the response queue name by appending the given transaction ID to the base response queue name.
     *
     * @param tranId
     *            the transaction ID to append to the response queue name
     * @return the full response queue name for the specified transaction
     */
    public String getResponseQueueName(String tranId) {
        return responseQueueName + tranId;
    }
}