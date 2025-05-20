package dev.seqism.core.processor;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.core.helper.CoreQueueHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BizProcessorSample001 implements BizProcessor<String> {
    @Override
    public String getBizCode() {
        return "Sample001";
    }

    @Override
    public void process(SeqismMessage<String> message, CoreQueueHelper queueHelper) {
        SeqismMessage.SeqismMessageHeader header = message.getHeader();
        String processed = "APDU1111";
        SeqismMessage<String> response = queueHelper.sendAndReceiveOrThrow(new SeqismMessage<>(header, processed));

        processed = response.getBody() + "_APDU2222";
        response = queueHelper.sendAndReceiveOrThrow(new SeqismMessage<>(header, processed));

        processed = response.getBody() + "_APDU3333";
        response = queueHelper.sendAndReceiveOrThrow(new SeqismMessage<>(header, processed));

        processed = response.getBody() + "_APDU4444";
        queueHelper.sendFinal(new SeqismMessage<>(header.toSuccess(), processed));
    }
}