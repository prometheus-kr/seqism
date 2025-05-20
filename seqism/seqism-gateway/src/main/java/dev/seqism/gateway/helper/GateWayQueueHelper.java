package dev.seqism.gateway.helper;

import dev.seqism.common.helper.QueueNameHelper;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GateWayQueueHelper {
    private static final int RECEIVE_TIME_OUT = 5000;
    private static final int QUEUE_DELETE_TIME = 60000;

    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final ParameterizedTypeReference<SeqismMessage<Object>> typeRef = new ParameterizedTypeReference<SeqismMessage<Object>>() {};

    public GateWayQueueHelper(RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public SeqismMessage<Object> sendAndReceiveInit(SeqismMessage<Object> msg) {
        log.debug("Sending message : [{}]", msg);
        
        createQueues(msg);

        rabbitTemplate.convertAndSend(QueueNameHelper.getStaticQueueName(), msg);
        return receive(msg);
    }

    public SeqismMessage<Object> sendAndReceiveNext(SeqismMessage<Object> msg) {
        log.debug("Sending message : [{}]", msg);
        String tranId = msg.getHeader().getTranId();

        rabbitTemplate.convertAndSend(QueueNameHelper.getResponseQueueName(tranId), msg);
        return receive(msg);
    }

    void createQueues(SeqismMessage<Object> msg) {
        String tranId = msg.getHeader().getTranId();

        String commandQueue = QueueNameHelper.getCommandQueueName(tranId);
        String responseQueue = QueueNameHelper.getResponseQueueName(tranId);

        declareQueue(commandQueue);
        declareQueue(responseQueue);
        log.debug("Created queues : [{}], [{}]", commandQueue, responseQueue);
    }

    void declareQueue(String queueName) {
        Queue queue = QueueBuilder.durable(queueName)
                .withArgument("x-expires", QUEUE_DELETE_TIME)
                .build();
        rabbitAdmin.declareQueue(queue);
    }

    SeqismMessage<Object> receive(SeqismMessage<Object> msg) {
        String tranId = msg.getHeader().getTranId();
        
        String commandQueue = QueueNameHelper.getCommandQueueName(tranId);
        String responseQueue = QueueNameHelper.getResponseQueueName(tranId);

        SeqismMessage<Object> receivedMsg = rabbitTemplate.receiveAndConvert(commandQueue, RECEIVE_TIME_OUT, typeRef);
        log.debug("Received message : [{}]", receivedMsg);

        // 성공 또는 실패 상태인 경우 큐 삭제
        if (receivedMsg == null || receivedMsg.getHeader().getStatus() != SeqismMessageStatus.IN_PROGRESS) {
            rabbitAdmin.deleteQueue(commandQueue);
            rabbitAdmin.deleteQueue(responseQueue);
            log.debug("Deleted queues : [{}], [{}]", commandQueue, responseQueue);
        }

        return receivedMsg;
    }
}