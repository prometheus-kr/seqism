package dev.seqism.gateway.endpoint;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.gateway.service.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * GatewayController
 *
 * @author seqism
 * @since 2025.05.16
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class GatewayController {
    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @PostMapping("/init")
    public ResponseEntity<SeqismMessage<Object>> initSeqism(@RequestBody SeqismMessage<Object> request) {
        SeqismMessage<Object> message = new SeqismMessage<>(
                SeqismMessage.SeqismMessageHeader.inProgress(request.getHeader().getBizCode(), generateTranId()),
                request.getBody()
        );
        SeqismMessage<Object> response = gatewayService.initSeqism(message);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/next")
    public ResponseEntity<SeqismMessage<Object>> nextSeqism(@RequestBody SeqismMessage<Object> request) {
        SeqismMessage<Object> message = new SeqismMessage<>(
                request.getHeader().toInProgress(),
                request.getBody()
        );
        SeqismMessage<Object> response = gatewayService.nextSeqism(message);
        return ResponseEntity.ok(response);
    }

    String generateTranId() {
        return UUID.randomUUID().toString();
    }
}