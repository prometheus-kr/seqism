package io.github.prometheuskr.seqism.ex.processor.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.prometheuskr.seqism.common.vo.SeqismMessage;
import io.github.prometheuskr.seqism.processor.SeqismProcessor;
import io.github.prometheuskr.seqism.processor.helper.ProcessorQueueHelper;

@Slf4j
@Component
public class SeqismProcessorSample001 extends SeqismProcessor<String, String> {

    public SeqismProcessorSample001(ObjectMapper mapper, ProcessorQueueHelper queueHelper) {
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
        String processed = message.getBody() + "=====> Command_1111";
        SeqismMessage<String> response = sendAndReceiveOrThrow(message.withBody(processed));

        processed = response.getBody() + "=====> Command_2222";
        response = sendAndReceiveOrThrow(response.withBody(processed));

        processed = response.getBody() + "=====> Command_3333";
        response = sendAndReceiveOrThrow(response.withBody(processed));

        processed = response.getBody() + "=====> Command_4444";
        sendFinal(response.withBody(processed).toSuccess());
    }
}