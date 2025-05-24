package dev.seqism.common.helper;

import dev.seqism.common.constant.SeqismConstant;

/**
 * Helper class for generating standardized queue names used within the application.
 * Provides methods to retrieve static, command, and response queue names based on transaction IDs.
 *
 * @author Prometheus-kr
 * @since 0.0.1
 */
public class QueueNameHelper {
    /**
     * Retrieves the name of the static queue used in the application.
     *
     * @return the static queue name defined in {@link SeqismConstant#SEQISM_STATIC_QUEUE}
     */
    public static String getStaticQueueName() {
        return SeqismConstant.SEQISM_STATIC_QUEUE;
    }

    /**
     * Generates a command queue name based on the provided transaction ID.
     *
     * @param tranId
     *            the transaction ID used to create a unique command queue name
     * @return the generated command queue name
     */
    public static String getCommandQueueName(String tranId) {
        return SeqismConstant.COMMAND_QUEUE_PREFIX + tranId;
    }

    /**
     * Generates a response queue name based on the provided transaction ID.
     *
     * @param tranId
     *            the transaction ID used to create a unique response queue name
     * @return the generated response queue name
     */
    public static String getResponseQueueName(String tranId) {
        return SeqismConstant.RESPONSE_QUEUE_PREFIX + tranId;
    }
}