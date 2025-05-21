package dev.seqism.gateway.service;

import dev.seqism.common.vo.ErrorInfo;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.gateway.helper.GateWayQueueHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.function.Function;

@Slf4j
@Service
public class GatewayService {
    private final GateWayQueueHelper queueHelper;

    public GatewayService(GateWayQueueHelper queueHelper) {
        this.queueHelper = queueHelper;
    }

    public SeqismMessage<Object> initSeqism(SeqismMessage<Object> message) {
        return sendAndReceive(message, queueHelper::sendAndReceiveInit);
    }

    public SeqismMessage<Object> nextSeqism(SeqismMessage<Object> message) {
        return sendAndReceive(message, queueHelper::sendAndReceiveNext);
    }

    SeqismMessage<Object> sendAndReceive(SeqismMessage<Object> message, Function<SeqismMessage<Object>, SeqismMessage<Object>> sender) {
        try {
            SeqismMessage<Object> response = sender.apply(message);
            if (response == null) {
                throw new IllegalStateException("No response from MQ (timeout) or invalid request");
            }
            return response;
        } catch (Exception e) {
            log.error("Error in GatewayService", e);
            return message.toFailure(ErrorInfo.ERROR_0001_0001, e.getMessage());
        }
    }
}