package dev.seqism.core;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BizProcessorBankIC implements BizProcessor {
    @Override
    public String getBizCode() {
        return "BankIC";
    }

    @Override
    public void process(SeqismMessage message, CoreQueueHelper queueHelper) {
        SeqismMessageHeader header = message.getHeader();
        String processed = "APDU1111";
        SeqismMessage response = queueHelper.sendAndReceiveOrThrow(new SeqismMessage(header, processed));

        processed = response.getMessage() + "_APDU2222";
        response = queueHelper.sendAndReceiveOrThrow(new SeqismMessage(header, processed));

        processed = response.getMessage() + "_APDU3333";
        response = queueHelper.sendAndReceiveOrThrow(new SeqismMessage(header, processed));

        processed = response.getMessage() + "_APDU4444";
        queueHelper.sendFinal(new SeqismMessage(header.toSuccess(), processed));
    }
}