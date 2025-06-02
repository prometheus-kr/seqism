package io.github.prometheuskr.seqism.ex.gateway.endpoint;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.github.prometheuskr.seqism.common.vo.SeqismMessage;
import io.github.prometheuskr.seqism.common.vo.SeqismMessageStatus;
import io.github.prometheuskr.seqism.gateway.service.GatewayService;

@Slf4j
@RestController
@RequestMapping("/api")
public class GatewayController {
    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @PostMapping("/init")
    public ResponseEntity<SeqismMessage<Object>> initSeqism(@RequestBody SeqismMessage<Object> message) {
        return buildResponseEntity(gatewayService.initSeqism(message));
    }

    @PostMapping("/next")
    public ResponseEntity<SeqismMessage<Object>> nextSeqism(@RequestBody SeqismMessage<Object> message) {
        return buildResponseEntity(gatewayService.nextSeqism(message));
    }

    ResponseEntity<SeqismMessage<Object>> buildResponseEntity(SeqismMessage<Object> message) {
        HttpStatusCode status = message.getHeader().getStatus() == SeqismMessageStatus.FAILURE
                ? HttpStatus.INTERNAL_SERVER_ERROR
                : HttpStatus.OK;

        return ResponseEntity.status(status).body(message);
    }
}