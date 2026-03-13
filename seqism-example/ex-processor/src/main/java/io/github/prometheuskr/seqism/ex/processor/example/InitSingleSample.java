package io.github.prometheuskr.seqism.ex.processor.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.prometheuskr.seqism.common.vo.SeqismMessage;
import io.github.prometheuskr.seqism.processor.SeqismProcessor;
import io.github.prometheuskr.seqism.processor.helper.ProcessorQueueHelper;

@Slf4j
@Component
public class InitSingleSample extends SeqismProcessor<String, String> {

    public InitSingleSample(ObjectMapper mapper, ProcessorQueueHelper queueHelper) {
        super(mapper, queueHelper);
    }

    @Override
    public String getBizCode() {
        return "InitSingle";
    }

    @Override
    public Class<String> getBodyType() {
        return String.class;
    }

    @Override
    public void process(SeqismMessage<String> message) {
        String result = message.getBody() + " =====> Init Single Done!";
        sendFinal(message.withBody(result).toSuccess());
    }

    @Override
    public SeqismMessage<?> processOnetime(SeqismMessage<String> message) {
        throw new UnsupportedOperationException("processOnetime is not supported for InitSingle");
    }
}
