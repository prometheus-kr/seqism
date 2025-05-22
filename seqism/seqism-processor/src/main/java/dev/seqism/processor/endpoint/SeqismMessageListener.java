package dev.seqism.processor.endpoint;

import dev.seqism.common.constant.SeqismConstant;
import dev.seqism.common.vo.ErrorInfo;
import dev.seqism.common.vo.SeqismException;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.processor.helper.CoreQueueHelper;
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
public abstract class SeqismMessageListener<T> {
    protected final CoreQueueHelper queueHelper;

    SeqismMessageListener(CoreQueueHelper queueHelper) {
        this.queueHelper = queueHelper;
    }

    @RabbitListener(queues = SeqismConstant.SEQISM_STATIC_QUEUE)
    public void handleMessage(SeqismMessage<T> message) {
        CompletableFuture.runAsync(() -> {
            try {
                this.proc(message);
            } catch (SeqismException e) {
                log.error("An exception occurred while processing", e);
                queueHelper.sendFinal((message.toFailure(e.getErrorInfo())));
            } catch (Exception e) {
                log.error("An exception occurred while processing", e);
                queueHelper.sendFinal((message.toFailure(ErrorInfo.ERROR_0002_0002, e.getMessage())));
            }
        });
    }

    abstract void proc(SeqismMessage<T> seqismMessage);
}