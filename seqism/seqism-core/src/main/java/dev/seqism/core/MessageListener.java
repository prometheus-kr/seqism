package dev.seqism.core;

import dev.seqism.common.constant.SeqismConstant;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageType;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * MessageListener
 *
 * @author seqism
 * @since 2025.05.16
 */
@Slf4j
@Component
public class MessageListener {
    private final CoreQueueHelper queueHelper;

    MessageListener(CoreQueueHelper queueHelper) {
        this.queueHelper = queueHelper;
    }

    @RabbitListener(queues = SeqismConstant.SEQISM_STATIC_QUEUE)
    public void handleMessage(SeqismMessage seqismMessage) {
        CompletableFuture.runAsync(() -> {
            try {
                this.proc(seqismMessage);
            } catch (Exception e) {
                log.error("An exception occurred while processing", e);
                queueHelper.sendFinal(new SeqismMessage(seqismMessage.getTranId(), SeqismMessageType.FAILURE, e.getMessage()));
            }
        });
    }

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