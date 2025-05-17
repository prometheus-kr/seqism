package dev.seqism.gateway;

import dev.seqism.common.vo.SeqismMessage;
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
        String tranId = "" + System.currentTimeMillis() + "-transaction-uuid";

        String tranCQueue = "tranC." + tranId;
        String tranRQueue = "tranR." + tranId;

        createQueue(tranCQueue);
        createQueue(tranRQueue);

        // MQ로 메시지 전달 (SeqismMessage 객체로)
        rabbitTemplate.convertAndSend("seqism-static-queue", new SeqismMessage(tranId, message));

        // response queue에서 응답 대기 (timeout: 5초)
        Object response = receiveSeqismMessage(tranCQueue);

        rabbitTemplate.convertAndSend(tranRQueue, new SeqismMessage(tranId, message + "___1111"));
        response = receiveSeqismMessage(tranCQueue);

        rabbitTemplate.convertAndSend(tranRQueue, new SeqismMessage(tranId, message + "___2222"));
        response = receiveSeqismMessage(tranCQueue);

        deleteQueue(tranCQueue);
        deleteQueue(tranRQueue);

        if (response instanceof SeqismMessage) {
            return ResponseEntity.ok("Response from MQ: " + ((SeqismMessage) response).getMessage());
        } else if (response != null) {
            return ResponseEntity.ok("Response from MQ: " + response.toString());
        } else {
            return ResponseEntity.status(504).body("No response from MQ (timeout)");
        }
    }

    private Object receiveSeqismMessage(String queueName) {
        return rabbitTemplate.receiveAndConvert(queueName, 5000);
    }

    private void createQueue(String queueName) {
        Queue queue = QueueBuilder.durable(queueName)
                .withArgument("x-expires", 60000)
                .build();
        rabbitAdmin.declareQueue(queue);
    }

    private void deleteQueue(String queueName) {
        rabbitAdmin.deleteQueue(queueName);
    }
}