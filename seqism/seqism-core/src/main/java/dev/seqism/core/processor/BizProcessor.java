package dev.seqism.core.processor;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.core.helper.CoreQueueHelper;

public interface BizProcessor<T> {
    String getBizCode();
    void process(SeqismMessage<T> message, CoreQueueHelper queueHelper);
}