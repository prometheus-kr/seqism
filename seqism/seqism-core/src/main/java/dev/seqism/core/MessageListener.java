package dev.seqism.core;

import dev.seqism.common.vo.SeqismMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * MessageListener
 *
 * @author seqism
 * @since 2025.05.16
 */
@Component
public class MessageListener {

    private final RabbitTemplate rabbitTemplate;

    MessageListener(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "seqism-static-queue")
    public void handleMessage(SeqismMessage seqismMessage) {
        String tranCQueue = "tranC." + seqismMessage.getTranId();
        String tranRQueue = "tranR." + seqismMessage.getTranId();

        Thread thread = new Thread(() -> {
            try {
                this.proc(tranCQueue, tranRQueue, seqismMessage.getMessage());
            } catch (Exception e) {
                System.err.println("Error receiving message: " + e.getMessage());
            }
        });

        thread.start();
    }

    private void proc(String tranCQueue, String tranRQueue, String message) {
        String processed = "core_data";
        rabbitTemplate.convertAndSend(tranCQueue, new SeqismMessage(extractTranId(tranCQueue), processed + "1111"));
        rabbitTemplate.receiveAndConvert(tranRQueue, 5000);

        rabbitTemplate.convertAndSend(tranCQueue, new SeqismMessage(extractTranId(tranCQueue), processed + "2222"));
        rabbitTemplate.receiveAndConvert(tranRQueue, 5000);

        rabbitTemplate.convertAndSend(tranCQueue, new SeqismMessage(extractTranId(tranCQueue), processed + "3333"));
    }

    private String extractTranId(String tranCQueue) {
        // "tranC.{tranId}"에서 tranId 추출
        int idx = tranCQueue.indexOf(".");
        return idx > 0 ? tranCQueue.substring(idx + 1) : tranCQueue;
    }
}