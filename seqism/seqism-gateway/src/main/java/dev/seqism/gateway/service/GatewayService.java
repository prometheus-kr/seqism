package dev.seqism.gateway.service;

import dev.seqism.common.vo.ErrorInfo;
import dev.seqism.common.vo.SeqismException;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.gateway.helper.GateWayQueueHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
public class GatewayService {
    private final GateWayQueueHelper queueHelper;

    public GatewayService(GateWayQueueHelper queueHelper) {
        this.queueHelper = queueHelper;
    }

    public <T> SeqismMessage<T> initSeqism(SeqismMessage<T> message) {
        return sendAndReceive(message.toInProgress(generateTranId()), queueHelper::sendAndReceiveInit);
    }

    public <T> SeqismMessage<T> nextSeqism(SeqismMessage<T> message) {
        return sendAndReceive(message.toInProgress(), queueHelper::sendAndReceiveNext);
    }

    String generateTranId() {
        return UUID.randomUUID().toString();
    }

    <T> SeqismMessage<T> sendAndReceive(SeqismMessage<T> message, Function<SeqismMessage<T>, SeqismMessage<T>> sender) {
        try {
            SeqismMessage<T> response = sender.apply(message);
            return response != null ? response : message.toFailure(ErrorInfo.ERROR_0001_0002);
        } catch (SeqismException e) {
            log.error("Error in GatewayService", e);
            return message.toFailure(e.getErrorInfo());
        } catch (Exception e) {
            log.error("Error in GatewayService", e);
            return message.toFailure(ErrorInfo.ERROR_0001_0001, e.getMessage());
        }
    }
}