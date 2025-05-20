package dev.seqism.common.helper;

import dev.seqism.common.constant.SeqismConstant;

public class QueueNameHelper {
    public static String getStaticQueueName() {
        return SeqismConstant.SEQISM_STATIC_QUEUE;
    }
    public static String getCommandQueueName(String tranId) {
        return SeqismConstant.COMMAND_QUEUE_PREFIX + tranId;
    }
    public static String getResponseQueueName(String tranId) {
        return SeqismConstant.RESPONSE_QUEUE_PREFIX + tranId;
    }
}