package dev.seqism.core.processor;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.core.helper.CoreQueueHelper;

public abstract class BizProcessor<T> {
    protected final CoreQueueHelper queueHelper;
    protected final ObjectMapper mapper;

    public BizProcessor(CoreQueueHelper queueHelper, ObjectMapper mapper) {
        this.queueHelper = queueHelper;
        this.mapper = mapper;
    }
    
    protected SeqismMessage<T> sendAndReceiveOrThrow(SeqismMessage<T> msg) {
        SeqismMessage<T> response = queueHelper.sendAndReceiveOrThrow(msg);
        T respBody = mapper.convertValue(response.getBody(), getBodyType());
        return new SeqismMessage<>(msg.getHeader(), respBody);
    }

    protected void sendFinal(SeqismMessage<T> msg) {
        queueHelper.sendFinal(msg);
    }

    public abstract String getBizCode();
    public abstract Class<T> getBodyType();
    public abstract void process(SeqismMessage<T> message);
}