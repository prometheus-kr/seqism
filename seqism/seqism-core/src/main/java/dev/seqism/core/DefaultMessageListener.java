package dev.seqism.core;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageType;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * DefaultMessageListener
 *
 * @author seqism
 * @since 2025.05.16
 */
@Slf4j
@Component
public class DefaultMessageListener extends SeqismMessageListener {

    DefaultMessageListener(CoreQueueHelper queueHelper) {
        super(queueHelper);
    }

    @Override
    void proc(SeqismMessage seqismMessage) {
        String tranId = seqismMessage.getTranId();

        String processed = "core_data1111";
        SeqismMessage response = queueHelper.sendAndReceiveOrThrow(new SeqismMessage(tranId, processed));

        processed = response.getMessage() + "2222";
        response = queueHelper.sendAndReceiveOrThrow(new SeqismMessage(tranId, processed));

        processed = response.getMessage() + "3333";
        response = queueHelper.sendAndReceiveOrThrow(new SeqismMessage(tranId, processed));

        processed = response.getMessage() + "4444";
        queueHelper.sendFinal(new SeqismMessage(tranId, SeqismMessageType.SUCCESS, processed));
    }
}