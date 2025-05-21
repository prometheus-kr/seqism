package dev.seqism.gateway.endpoint;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageHeader;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageStatus;
import dev.seqism.gateway.service.GatewayService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
        SeqismMessage<Object> response = gatewayService.initSeqism(request);
        return buildResponseEntity(response);
    }

    @PostMapping("/next")
    public ResponseEntity<SeqismMessage<Object>> nextSeqism(@RequestBody SeqismMessage<Object> request) {
        SeqismMessage<Object> response = gatewayService.nextSeqism(request);
        return buildResponseEntity(response);
    }

    ResponseEntity<SeqismMessage<Object>> buildResponseEntity(SeqismMessage<Object> message) {
        HttpStatusCode status = message.getHeader().getStatus() == SeqismMessageStatus.FAILURE
                ? HttpStatus.INTERNAL_SERVER_ERROR
                : HttpStatus.OK;

        return ResponseEntity.status(status).body(message);
    }
}