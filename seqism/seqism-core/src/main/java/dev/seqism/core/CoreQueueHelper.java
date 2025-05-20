package dev.seqism.core;

import dev.seqism.common.QueueNameHelper;
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
    private final ParameterizedTypeReference<SeqismMessage> typeRef = new ParameterizedTypeReference<SeqismMessage>() {};

    public CoreQueueHelper(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public SeqismMessage sendAndReceiveOrThrow(SeqismMessage msg) {
        sendMessage(msg);

        return receivedMessage(msg);
    }

    public void sendFinal(SeqismMessage msg) {
        sendMessage(msg);
    }

    void sendMessage(SeqismMessage msg) {
        log.debug("Sending message : [{}]", msg);

        rabbitTemplate.convertAndSend(QueueNameHelper.getCommandQueueName(msg.getHeader().getTranId()), msg);
    }

    SeqismMessage receivedMessage(SeqismMessage msg) {
        String responseQueueName = QueueNameHelper.getResponseQueueName(msg.getHeader().getTranId());

        SeqismMessage receivedMsg = rabbitTemplate.receiveAndConvert(responseQueueName, RECEIVE_TIME_OUT, typeRef);
        if (receivedMsg == null) {
            log.error("Timeout occurred while waiting for response from queue : [{}]", responseQueueName);
            throw new RuntimeException("Timeout occurred while waiting for response from queue");
        }

        log.debug("Received message : [{}]", receivedMsg);
        return receivedMsg;
    }
}