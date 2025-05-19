package dev.seqism.gateway;

import dev.seqism.common.constant.SeqismConstant;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageType;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
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
        return initSeqism0(request.get("message"));
    }

    @PostMapping("/next")
    public ResponseEntity<SeqismMessage> nextSeqism(@RequestBody Map<String, String> request) {
        return nextSeqism0(request.get("tranId"), request.get("message"));
    }

    ResponseEntity<SeqismMessage> initSeqism0(@Nullable String message) {
        try {
            SeqismMessage response = queueHelper.sendAndReceiveInit(message);
            return buildResponseEntity(response);
        } catch (Exception e) {
            log.error("Error in initSeqism", e);
            return buildResponseEntity("Internal error : " + e.getMessage());
        }
    }

    ResponseEntity<SeqismMessage> nextSeqism0(String tranId, String message) {
        try {
            if (tranId == null || tranId.isBlank() || message == null || message.isBlank()) {
                throw new IllegalArgumentException("tranId and message must not be null or blank");
            }

            SeqismMessage response = queueHelper.sendAndReceiveNext(new SeqismMessage(tranId, message));

            return buildResponseEntity(response);
        } catch (Exception e) {
            log.error("Error in nextSeqism", e);
            return buildResponseEntity("Internal error : " + e.getMessage());
        }
    }

    ResponseEntity<SeqismMessage> buildResponseEntity(SeqismMessage response) {
        SeqismMessage result = response != null
                ? response
                : new SeqismMessage("No response from MQ (timeout) or invalid request");
        return ResponseEntity.ok(result);
    }

    ResponseEntity<SeqismMessage> buildResponseEntity(String errorMessage) {
        return ResponseEntity.ok(new SeqismMessage(errorMessage));
    }
}