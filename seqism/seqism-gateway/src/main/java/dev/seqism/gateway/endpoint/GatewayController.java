package dev.seqism.gateway.endpoint;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.gateway.helper.GateWayQueueHelper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    private final GateWayQueueHelper queueHelper;

    public GatewayController(GateWayQueueHelper queueHelper) {
        this.queueHelper = queueHelper;
    }

    @PostMapping("/init")
    public ResponseEntity<SeqismMessage<Object>> initSeqism(@RequestBody SeqismMessage<Object> request) {
        return initSeqism0(new SeqismMessage<>(SeqismMessage.SeqismMessageHeader.inProgress(request.getHeader().getBizCode(), generateTranId()), request.getBody()));
    }

    @PostMapping("/next")
    public ResponseEntity<SeqismMessage<Object>> nextSeqism(@RequestBody SeqismMessage<Object> request) {
        return nextSeqism0(new SeqismMessage<>(request.getHeader().toInProgress(), request.getBody()));
    }

    String generateTranId() {
        return UUID.randomUUID().toString();
    }

    ResponseEntity<SeqismMessage<Object>> initSeqism0(SeqismMessage<Object> message) {
        try {
            SeqismMessage<Object> response = queueHelper.sendAndReceiveInit(message);
            if (response == null) {
                throw new IllegalStateException("No response from MQ (timeout) or invalid request");
            }

            return buildResponseEntity(response);
        } catch (Exception e) {
            log.error("Error in initSeqism", e);
            return buildFailureResponseEntity(message.getHeader(), "Internal error : " + e.getMessage());
        }
    }

    ResponseEntity<SeqismMessage<Object>> nextSeqism0(SeqismMessage<Object> message) {
        try {
            SeqismMessage<Object> response = queueHelper.sendAndReceiveNext(message);
            if (response == null) {
                throw new IllegalStateException("No response from MQ (timeout) or invalid request");
            }

            return buildResponseEntity(response);
        } catch (Exception e) {
            log.error("Error in nextSeqism", e);
            return buildFailureResponseEntity(message.getHeader(), "Internal error : " + e.getMessage());
        }
    }

    ResponseEntity<SeqismMessage<Object>> buildResponseEntity(SeqismMessage<Object> message) {
        return ResponseEntity.ok(message);
    }

    ResponseEntity<SeqismMessage<Object>> buildFailureResponseEntity(SeqismMessage.SeqismMessageHeader header, String errorMessage) {
        return ResponseEntity.ok(new SeqismMessage<>(header.toFailure(), errorMessage));
    }
}