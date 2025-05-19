package dev.seqism.core;

import dev.seqism.common.constant.SeqismConstant;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageType;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

import org.springframework.amqp.rabbit.annotation.RabbitListener;

/**
 * MessageListener
 *
 * @author seqism
 * @since 2025.05.16
 */
@Slf4j
public abstract class SeqismMessageListener {
    protected final CoreQueueHelper queueHelper;

    SeqismMessageListener(CoreQueueHelper queueHelper) {
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

    abstract void proc(SeqismMessage seqismMessage);
}