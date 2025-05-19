package dev.seqism.gateway;

import dev.seqism.common.constant.SeqismConstant;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageType;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Slf4j
@Component
public class GateWayQueueHelper {
    private static final int RECEIVE_TIME_OUT = 5000;
    private static final int QUEUE_DELETE_TIME = 60000;

    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final ParameterizedTypeReference<SeqismMessage> typeRef = new ParameterizedTypeReference<SeqismMessage>() {};

    public GateWayQueueHelper(RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public SeqismMessage sendAndReceiveInit(String message) {
        String tranId = generateTranId();
        log.debug("tranId : [{}]", tranId);

        createQueues(tranId);

        SeqismMessage msg = new SeqismMessage(tranId, message);
        log.debug("Sending message : [{}]", msg);

        rabbitTemplate.convertAndSend(SeqismConstant.SEQISM_STATIC_QUEUE, msg);
        return receive(tranId);
    }

    public SeqismMessage sendAndReceiveNext(SeqismMessage msg) {
        log.debug("Sending message : [{}]", msg);
        String tranId = msg.getTranId();

        rabbitTemplate.convertAndSend(getResponseQueueName(tranId), msg);
        return receive(tranId);
    }

    String generateTranId() {
        return UUID.randomUUID().toString();
    }

    void createQueues(String tranId) {
        declareQueue(getCommandQueueName(tranId));
        declareQueue(getResponseQueueName(tranId));
    }

    String getCommandQueueName(String tranId) {
        return SeqismConstant.COMMAND_QUEUE_PREFIX + tranId;
    }

    String getResponseQueueName(String tranId) {
        return SeqismConstant.RESPONSE_QUEUE_PREFIX + tranId;
    }

    void declareQueue(String queueName) {
        Queue queue = QueueBuilder.durable(queueName)
                .withArgument("x-expires", QUEUE_DELETE_TIME)
                .build();
        rabbitAdmin.declareQueue(queue);
    }

    private SeqismMessage receive(String tranId) {
        SeqismMessage receivedMsg = rabbitTemplate.receiveAndConvert(getCommandQueueName(tranId), RECEIVE_TIME_OUT, typeRef);
        log.debug("Received message : [{}]", receivedMsg);

        deleteQueuesIfNecessary(receivedMsg, tranId);

        return receivedMsg;
    }

    void deleteQueuesIfNecessary(SeqismMessage msg, String tranId) {
        if (msg == null || msg.getType() != SeqismMessageType.IN_PROGRESS) {
            rabbitAdmin.deleteQueue(getResponseQueueName(tranId));
            rabbitAdmin.deleteQueue(getCommandQueueName(tranId));
        }
    }
}