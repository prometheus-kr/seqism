package dev.seqism.gateway;

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

    private final RabbitTemplate rabbitTemplate;

    public GatewayController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        System.out.println("Received message: " + message);

        // MQ로 메시지 전달
        rabbitTemplate.convertAndSend("seqism-exchange", "seqism-request-routing-key", message);

        // response queue에서 응답 대기 (timeout: 5초)
        Object response = rabbitTemplate.receiveAndConvert("seqism-response-queue", 5000);

        if (response != null) {
            return ResponseEntity.ok("Response from MQ: " + response.toString());
        } else {
            return ResponseEntity.status(504).body("No response from MQ (timeout)");
        }
    }
}