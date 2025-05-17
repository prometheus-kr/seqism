package dev.seqism.gateway;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * GatewayController
 *
 * @author seqism
 * @since 2025.05.16
 */
@RestController
@RequestMapping("/api")
public class GatewayController {

    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;

    public GatewayController(RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        System.out.println("[1] Gateway: Received message: " + message);

        String tranId = "" + System.currentTimeMillis() + "-transaction-uuid";

        String tranCQueue = "tranC." + tranId;
        String tranRQueue = "tranR." + tranId;

        // 1. 트랜잭션 전용 큐 생성
        createQueue(tranCQueue);
        createQueue(tranRQueue);

        // MQ로 메시지 전달
        System.out.println("[2] Gateway: Sending to core via seqism-static-queue: " + tranId);
        rabbitTemplate.convertAndSend("seqism-static-queue", tranId);

        // response queue에서 응답 대기 (timeout: 5초)
        System.out.println("[5] Gateway: Waiting for response on " + tranCQueue);
        Object response = rabbitTemplate.receiveAndConvert(tranCQueue, 5000);
        System.out.println("[8] Gateway: Response from MQ: " + response);

        System.out.println("[9] Gateway: Sending to " + tranRQueue + ": " + message + "___1111");
        rabbitTemplate.convertAndSend(tranRQueue, message + "___1111");

        System.out.println("[12] Gateway: Waiting for response on " + tranCQueue);
        response = rabbitTemplate.receiveAndConvert(tranCQueue, 5000);
        System.out.println("[15] Gateway: Response from MQ: " + response);

        System.out.println("[16] Gateway: Sending to " + tranRQueue + ": " + message + "___2222");
        rabbitTemplate.convertAndSend(tranRQueue, message + "___2222");

        System.out.println("[19] Gateway: Waiting for response on " + tranCQueue);
        response = rabbitTemplate.receiveAndConvert(tranCQueue, 5000);
        System.out.println("[22] Gateway: Response from MQ: " + response);

        deleteQueue(tranCQueue);
        deleteQueue(tranRQueue);

        if (response != null) {
            return ResponseEntity.ok("Response from MQ: " + response.toString());
        } else {
            return ResponseEntity.status(504).body("No response from MQ (timeout)");
        }
    }

    private void createQueue(String queueName) {
        Queue queue = QueueBuilder.durable(queueName)
                .withArgument("x-expires", 60000) // 1분 후 미사용 시 삭제
                .build();
        rabbitAdmin.declareQueue(queue);
    }

    private void deleteQueue(String queueName) {
        rabbitAdmin.deleteQueue(queueName);
    }
}