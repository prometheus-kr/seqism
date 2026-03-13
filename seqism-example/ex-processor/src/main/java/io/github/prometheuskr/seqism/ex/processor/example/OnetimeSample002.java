package io.github.prometheuskr.seqism.ex.processor.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

import io.github.prometheuskr.seqism.common.vo.SeqismMessage;
import io.github.prometheuskr.seqism.processor.SeqismProcessor;
import io.github.prometheuskr.seqism.processor.helper.ProcessorQueueHelper;

@Slf4j
@Component
public class OnetimeSample002
        extends SeqismProcessor<OnetimeSample002.OnetimeBody, OnetimeSample002.OnetimeBody> {

    public OnetimeSample002(ObjectMapper mapper, ProcessorQueueHelper queueHelper) {
        super(mapper, queueHelper);
    }

    @Override
    public String getBizCode() {
        return "Onetime002";
    }

    @Override
    public Class<OnetimeBody> getBodyType() {
        return OnetimeBody.class;
    }

    @Override
    public void process(SeqismMessage<OnetimeBody> message) {
        throw new UnsupportedOperationException("process is not supported for Onetime002");
    }

    @Override
    public SeqismMessage<?> processOnetime(SeqismMessage<OnetimeBody> message) {
        OnetimeBody body = message.getBody();
        OnetimeBody result = new OnetimeBody(body.getName(), body.getValue() * 2, "COMPLETED");
        return message.withBody(result).toSuccess();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnetimeBody implements Serializable {
        private String name;
        private int value;
        private String status;
    }
}
