package dev.seqism.core;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void handleMessage(String message) {
        System.out.println("[3] Core: Received message from MQ: " + message);

        String tranCQueue = "tranC." + message;
        String tranRQueue = "tranR." + message;

        Thread thread = new Thread(() -> {
            try {
                this.proc(tranCQueue, tranRQueue);
            } catch (Exception e) {
                System.err.println("Error receiving message: " + e.getMessage());
            }
        });

        thread.start();
    }

    private void proc(String tranCQueue, String tranRQueue) {
        System.out.println("[4] Core: Start processing");

        // 비즈니스 로직 실행
        String processed = "core_data";
        System.out.println("[6] Core data: " + processed);

        // 응답 메시지를 별도의 큐에 전송
        rabbitTemplate.convertAndSend(tranCQueue, processed + "1111");
        System.out.println("[7] Core: Sent response " + processed + "1111");

        Object r = rabbitTemplate.receiveAndConvert(tranRQueue, 5000);
        System.out.println("[10] Core: Received from " + tranRQueue + ": " + r);

        // 응답 메시지를 별도의 큐에 전송
        rabbitTemplate.convertAndSend(tranCQueue, processed + "2222");
        System.out.println("[13] Core: Sent response " + processed + "2222");

        r = rabbitTemplate.receiveAndConvert(tranRQueue, 5000);
        System.out.println("[17] Core: Received from " + tranRQueue + ": " + r);

        // 응답 메시지를 별도의 큐에 전송
        rabbitTemplate.convertAndSend(tranCQueue, processed + "3333");
        System.out.println("[20] Core: Sent response " + processed + "3333");
    }
}