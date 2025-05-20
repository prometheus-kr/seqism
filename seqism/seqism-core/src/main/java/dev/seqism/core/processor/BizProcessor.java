package dev.seqism.core.processor;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.core.helper.CoreQueueHelper;

public interface BizProcessor {
    String getBizCode();
    void process(SeqismMessage message, CoreQueueHelper queueHelper);
}