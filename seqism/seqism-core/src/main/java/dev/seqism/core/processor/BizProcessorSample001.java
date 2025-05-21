package dev.seqism.core.processor;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.core.helper.CoreQueueHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class BizProcessorSample001 extends BizProcessor<String> {

    public BizProcessorSample001(CoreQueueHelper queueHelper, ObjectMapper mapper) {
        super(queueHelper, mapper);
    }

    @Override
    public String getBizCode() {
        return "Sample001";
    }

    @Override
    public Class<String> getBodyType() {
        return String.class;
    }

    @Override
    public void process(SeqismMessage<String> message) {
        SeqismMessage.SeqismMessageHeader header = message.getHeader();
        String processed = "=====> Command_1111";
        SeqismMessage<String> response = sendAndReceiveOrThrow(new SeqismMessage<>(header, processed));

        processed = response.getBody() + "=====> Command_2222";
        response = sendAndReceiveOrThrow(new SeqismMessage<>(header, processed));

        processed = response.getBody() + "=====> Command_3333";
        response = sendAndReceiveOrThrow(new SeqismMessage<>(header, processed));

        processed = response.getBody() + "=====> Command_4444";
        sendFinal(new SeqismMessage<>(header.toSuccess(), processed));
    }
}