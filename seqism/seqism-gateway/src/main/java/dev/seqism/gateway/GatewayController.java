package dev.seqism.gateway;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageHeader;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
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
    public ResponseEntity<SeqismMessage> initSeqism(@RequestBody Map<String, String> request) {
        return initSeqism0(SeqismMessageHeader.inProgress(request.get("bizCode"), generateTranId()),
                request.get("message"));
    }

    @PostMapping("/next")
    public ResponseEntity<SeqismMessage> nextSeqism(@RequestBody Map<String, String> request) {
        return nextSeqism0(SeqismMessageHeader.inProgress(request.get("bizCode"), request.get("tranId")),
                request.get("message"));
    }

    String generateTranId() {
        return UUID.randomUUID().toString();
    }

    ResponseEntity<SeqismMessage> initSeqism0(SeqismMessageHeader header, String message) {
        try {
            SeqismMessage response = queueHelper.sendAndReceiveInit(new SeqismMessage(header, message));
            if (response == null) {
                throw new IllegalStateException("No response from MQ (timeout) or invalid request");
            }
            
            return buildResponseEntity(response);
        } catch (Exception e) {
            log.error("Error in initSeqism", e);
            return buildFailureResponseEntity(header, "Internal error : " + e.getMessage());
        }
    }

    ResponseEntity<SeqismMessage> nextSeqism0(SeqismMessageHeader header, String message) {
        try {
            SeqismMessage response = queueHelper.sendAndReceiveNext(new SeqismMessage(header, message));
            if (response == null) {
                throw new IllegalStateException("No response from MQ (timeout) or invalid request");
            }

            return buildResponseEntity(response);
        } catch (Exception e) {
            log.error("Error in nextSeqism", e);
            return buildFailureResponseEntity(header, "Internal error : " + e.getMessage());
        }
    }

    ResponseEntity<SeqismMessage> buildResponseEntity(SeqismMessage response) {
        return ResponseEntity.ok(response);
    }

    ResponseEntity<SeqismMessage> buildFailureResponseEntity(SeqismMessageHeader header, String errorMessage) {
        return ResponseEntity.ok(new SeqismMessage(header.toFailure(), errorMessage));
    }
}