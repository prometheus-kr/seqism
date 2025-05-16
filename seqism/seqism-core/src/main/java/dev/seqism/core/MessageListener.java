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

    @RabbitListener(queues = "seqism-request-queue")
    public void handleMessage(String message) {
        System.out.println("Received message from MQ: " + message);

        // 비즈니스 로직 실행
        String processed = processMessage(message);

        // 응답 메시지를 별도의 큐에 전송
        rabbitTemplate.convertAndSend("seqism-exchange", "seqism-response-routing-key", processed);
    }

    private String processMessage(String message) {
        String result = "*" + message + "*";
        System.out.println("Processing message: " + result);
        // 실제 업무 처리 로직 구현
        return result;
    }
}