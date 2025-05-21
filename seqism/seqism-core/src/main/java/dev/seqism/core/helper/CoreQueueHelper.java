package dev.seqism.core.helper;

import dev.seqism.common.constant.SeqismConstant;
import dev.seqism.common.helper.QueueNameHelper;
import dev.seqism.common.vo.ErrorInfo;
import dev.seqism.common.vo.SeqismException;
import dev.seqism.common.vo.SeqismMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoreQueueHelper {
    private final RabbitTemplate rabbitTemplate;

    public CoreQueueHelper(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public <T> SeqismMessage<T> sendAndReceiveOrThrow(SeqismMessage<T> message) {
        sendMessage(message);
        return receivedMessage(message);
    }

    public <T> void sendFinal(SeqismMessage<T> message) {
        sendMessage(message);
    }

    private <T> void sendMessage(SeqismMessage<T> message) {
        log.debug("Sending message : [{}]", message);
        String tranId = message.getHeader().getTranId();

        rabbitTemplate.convertAndSend(QueueNameHelper.getCommandQueueName(tranId), message);
    }

    private <T> SeqismMessage<T> receivedMessage(SeqismMessage<T> message) {
        String tranId = message.getHeader().getTranId();
        String responseQueueName = QueueNameHelper.getResponseQueueName(tranId);
        ParameterizedTypeReference<SeqismMessage<T>> typeRef = new ParameterizedTypeReference<SeqismMessage<T>>(){};
        
        SeqismMessage<T> receivedMsg = rabbitTemplate.receiveAndConvert(responseQueueName, SeqismConstant.RECEIVE_TIME_OUT, typeRef);
        if (receivedMsg == null) {
            log.error("Timeout occurred while waiting for response from queue : [{}]", responseQueueName);
            throw new SeqismException(ErrorInfo.ERROR_0002_0003);
        }
        
        log.debug("Received message : [{}]", receivedMsg);
        return receivedMsg;
    }
}