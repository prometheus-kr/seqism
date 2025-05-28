package dev.seqism.ex.processor.example;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.processor.SeqismProcessor;
import dev.seqism.processor.helper.ProcessorQueueHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class SeqismProcessorSample004
        extends SeqismProcessor<SeqismProcessorSample004.BodyR, SeqismProcessorSample004.BodyC> {

    public SeqismProcessorSample004(ObjectMapper mapper, ProcessorQueueHelper queueHelper) {
        super(mapper, queueHelper);
    }

    @Override
    public String getBizCode() {
        return "Sample004";
    }

    @Override
    public Class<BodyR> getBodyType() {
        return BodyR.class;
    }

    @Override
    public void process(SeqismMessage<BodyR> message) {
        // 입력 메시지 로깅
        log.info("Received message: {}", message);

        BodyC commandBody;
        commandBody = new BodyC("A", "11111111111");

        // 응답 메시지 생성 및 전송
        SeqismMessage<BodyC> command = message.withBody(commandBody);
        SeqismMessage<BodyR> abc = queueHelper.sendAndReceiveOrThrow(command);
        log.info("Response received: {}", abc);

        commandBody = new BodyC("B", "22222222222");
        command = message.withBody(commandBody);
        abc = queueHelper.sendAndReceiveOrThrow(command);
        log.info("Response received: {}", abc);

        commandBody = new BodyC("C", "33333333333");
        command = message.withBody(commandBody);
        abc = queueHelper.sendAndReceiveOrThrow(command);
        log.info("Response received: {}", abc);

        commandBody = new BodyC("D", "44444444444");
        command = message.withBody(commandBody).toSuccess();
        queueHelper.sendFinal(command);
    }

    @Data
    public static class BodyC implements Serializable {
        private final String result;
        private final String ssString;
    }

    @Data
    public static class BodyR implements Serializable {
        private final String number;
    }
}