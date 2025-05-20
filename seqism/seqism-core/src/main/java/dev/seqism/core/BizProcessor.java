package dev.seqism.core;
import dev.seqism.common.vo.SeqismMessage;

public interface BizProcessor {
    String getBizCode();
    void process(SeqismMessage message, CoreQueueHelper queueHelper);
}