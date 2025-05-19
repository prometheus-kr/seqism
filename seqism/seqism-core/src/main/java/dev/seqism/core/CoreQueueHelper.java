package dev.seqism.core;

import dev.seqism.common.constant.SeqismConstant;
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
    private final ParameterizedTypeReference<SeqismMessage> seqismMessageTypeRef = new ParameterizedTypeReference<SeqismMessage>() {};

    public CoreQueueHelper(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public SeqismMessage sendAndReceiveOrThrow(SeqismMessage msg) {
        log.debug("Sending message : [{}]", msg);
        String tranId = msg.getTranId();
        
        rabbitTemplate.convertAndSend(getCommandQueueName(tranId), msg);
        SeqismMessage receivedMsg = rabbitTemplate.receiveAndConvert(getResponseQueueName(tranId), RECEIVE_TIME_OUT, seqismMessageTypeRef);
        log.debug("Received message : [{}]", receivedMsg);
        if (receivedMsg == null) {
            log.error("Timeout occurred while waiting for response from queue : [{}]", getResponseQueueName(tranId));
            throw new RuntimeException("Timeout occurred while waiting for response from queue");
        }

        return receivedMsg;
    }

    public void sendFinal(SeqismMessage msg) {
        log.debug("Final Sending message : [{}]", msg);
        rabbitTemplate.convertAndSend(getCommandQueueName(msg.getTranId()), msg);
    }

    String getCommandQueueName(String tranId) {
        return SeqismConstant.COMMAND_QUEUE_PREFIX + tranId;
    }

    String getResponseQueueName(String tranId) {
        return SeqismConstant.RESPONSE_QUEUE_PREFIX + tranId;
    }
}