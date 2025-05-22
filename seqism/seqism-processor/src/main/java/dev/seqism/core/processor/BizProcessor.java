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
    
    protected SeqismMessage<T> sendAndReceiveOrThrow(SeqismMessage<T> message) {
        SeqismMessage<T> response = queueHelper.sendAndReceiveOrThrow(message);
        T respBody = mapper.convertValue(response.getBody(), getBodyType());
        return response.withBody(respBody);
    }

    protected void sendFinal(SeqismMessage<T> message) {
        queueHelper.sendFinal(message);
    }

    public abstract String getBizCode();
    public abstract Class<T> getBodyType();
    public abstract void process(SeqismMessage<T> message);
}