package dev.seqism.ex.processor.example;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.processor.SeqismProcessor;
import dev.seqism.processor.helper.CoreQueueHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class SeqismProcessorSample001 extends SeqismProcessor<String> {

    public SeqismProcessorSample001(ObjectMapper mapper, CoreQueueHelper queueHelper) {
        super(mapper, queueHelper);
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
        String processed = "=====> Command_1111";
        SeqismMessage<String> response = sendAndReceiveOrThrow(message.withBody(processed));

        processed = response.getBody() + "=====> Command_2222";
        response = sendAndReceiveOrThrow(response.withBody(processed));

        processed = response.getBody() + "=====> Command_3333";
        response = sendAndReceiveOrThrow(response.withBody(processed));

        processed = response.getBody() + "=====> Command_4444";
        sendFinal(response.withBody(processed).toSuccess());
    }
}