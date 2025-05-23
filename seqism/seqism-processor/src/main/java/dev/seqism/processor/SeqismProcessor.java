package dev.seqism.processor;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.processor.helper.CoreQueueHelper;

public abstract class SeqismProcessor<T> {
    protected final ObjectMapper mapper;
    protected final CoreQueueHelper queueHelper;

    public SeqismProcessor(ObjectMapper mapper, CoreQueueHelper queueHelper) {
        this.mapper = mapper;
        this.queueHelper = queueHelper;
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