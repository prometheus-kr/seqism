package dev.seqism.gateway.endpoint;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageBody;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageHeader;
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
    public ResponseEntity<SeqismMessage> initSeqism(@RequestBody SeqismMessage request) {
        return initSeqism0(new SeqismMessage(SeqismMessageHeader.inProgress(request.getHeader().getBizCode(), generateTranId()), request.getBody()));
    }

    @PostMapping("/next")
    public ResponseEntity<SeqismMessage> nextSeqism(@RequestBody SeqismMessage request) {
        return nextSeqism0(new SeqismMessage(request.getHeader().toInProgress(), request.getBody()));
    }

    String generateTranId() {
        return UUID.randomUUID().toString();
    }

    ResponseEntity<SeqismMessage> initSeqism0(SeqismMessage message) {
        try {
            SeqismMessage response = queueHelper.sendAndReceiveInit(message);
            if (response == null) {
                throw new IllegalStateException("No response from MQ (timeout) or invalid request");
            }

            return buildResponseEntity(response);
        } catch (Exception e) {
            log.error("Error in initSeqism", e);
            return buildFailureResponseEntity(message.getHeader(), "Internal error : " + e.getMessage());
        }
    }

    ResponseEntity<SeqismMessage> nextSeqism0(SeqismMessage message) {
        try {
            SeqismMessage response = queueHelper.sendAndReceiveNext(message);
            if (response == null) {
                throw new IllegalStateException("No response from MQ (timeout) or invalid request");
            }

            return buildResponseEntity(response);
        } catch (Exception e) {
            log.error("Error in nextSeqism", e);
            return buildFailureResponseEntity(message.getHeader(), "Internal error : " + e.getMessage());
        }
    }

    ResponseEntity<SeqismMessage> buildResponseEntity(SeqismMessage message) {
        return ResponseEntity.ok(message);
    }

    ResponseEntity<SeqismMessage> buildFailureResponseEntity(SeqismMessageHeader header, String errorMessage) {
        return ResponseEntity.ok(new SeqismMessage(header.toFailure(), new SeqismMessageBody(errorMessage)));
    }
}