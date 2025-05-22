package dev.seqism.processor.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.processor.helper.CoreQueueHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BizProcessorSample002 extends BizProcessor<BizProcessorSample002.Sample002Body> {

    public BizProcessorSample002(CoreQueueHelper queueHelper, ObjectMapper mapper) {
        super(queueHelper, mapper);
    }

    @Override
    public String getBizCode() {
        return "Sample002";
    }

    @Override
    public Class<Sample002Body> getBodyType() {
        return Sample002Body.class;
    }

    @Override
    public void process(SeqismMessage<Sample002Body> message) {
        Sample002Body body = new Sample002Body("[Sample002] Step1", 1);

        SeqismMessage<Sample002Body> response = sendAndReceiveOrThrow(message.withBody(body));

        body = new Sample002Body(response.getBody().getLog() + " -> [Sample002] Step2", 2);
        response = sendAndReceiveOrThrow(response.withBody(body));

        body = new Sample002Body(response.getBody().getLog() + " -> [Sample002] Step3", 3);
        response = sendAndReceiveOrThrow(response.withBody(body));

        body = new Sample002Body(response.getBody().getLog() + " -> [Sample002] Done", 4);
        sendFinal(response.withBody(body).toSuccess());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sample002Body implements Serializable {
        private String log;
        private int step;
    }
}