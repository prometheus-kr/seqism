package dev.seqism.core.helper;

import dev.seqism.common.helper.QueueNameHelper;
import dev.seqism.common.vo.SeqismMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoreQueueHelper {

    private static final int RECEIVE_TIME_OUT = 5000;
    private final RabbitTemplate rabbitTemplate;

    public CoreQueueHelper(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public <T> SeqismMessage<T> sendAndReceiveOrThrow(SeqismMessage<T> msg) {
        sendMessage(msg);
        return receivedMessage(msg);
    }

    public <T> void sendFinal(SeqismMessage<T> msg) {
        sendMessage(msg);
    }

    private <T> void sendMessage(SeqismMessage<T> msg) {
        log.debug("Sending message : [{}]", msg);
        String tranId = msg.getHeader().getTranId();

        rabbitTemplate.convertAndSend(QueueNameHelper.getCommandQueueName(tranId), msg);
    }

    private <T> SeqismMessage<T> receivedMessage(SeqismMessage<T> msg) {
        String tranId = msg.getHeader().getTranId();
        String responseQueueName = QueueNameHelper.getResponseQueueName(tranId);
        ParameterizedTypeReference<SeqismMessage<T>> typeRef = new ParameterizedTypeReference<SeqismMessage<T>>(){};
        
        SeqismMessage<T> receivedMsg = rabbitTemplate.receiveAndConvert(responseQueueName, RECEIVE_TIME_OUT, typeRef);
        if (receivedMsg == null) {
            log.error("Timeout occurred while waiting for response from queue : [{}]", responseQueueName);
            throw new RuntimeException("Timeout occurred while waiting for response from queue");
        }
        
        log.debug("Received message : [{}]", receivedMsg);
        return receivedMsg;
    }
}