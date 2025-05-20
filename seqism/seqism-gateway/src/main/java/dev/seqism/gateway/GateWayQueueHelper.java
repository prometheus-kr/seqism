package dev.seqism.gateway;

import dev.seqism.common.QueueNameHelper;
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
    private final ParameterizedTypeReference<SeqismMessage> typeRef = new ParameterizedTypeReference<SeqismMessage>() {};

    public GateWayQueueHelper(RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public SeqismMessage sendAndReceiveInit(SeqismMessage msg) {
        log.debug("Sending message : [{}]", msg);
        
        createQueues(msg);

        rabbitTemplate.convertAndSend(QueueNameHelper.getStaticQueueName(), msg);
        return receive(msg);
    }

    public SeqismMessage sendAndReceiveNext(SeqismMessage msg) {
        log.debug("Sending message : [{}]", msg);

        rabbitTemplate.convertAndSend(QueueNameHelper.getResponseQueueName(msg.getHeader().getTranId()), msg);
        return receive(msg);
    }

    void createQueues(SeqismMessage msg) {
        String commandQueue = QueueNameHelper.getCommandQueueName(msg.getHeader().getTranId());
        String responseQueue = QueueNameHelper.getResponseQueueName(msg.getHeader().getTranId());

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

    SeqismMessage receive(SeqismMessage msg) {
        String commandQueue = QueueNameHelper.getCommandQueueName(msg.getHeader().getTranId());
        String responseQueue = QueueNameHelper.getResponseQueueName(msg.getHeader().getTranId());

        SeqismMessage receivedMsg = rabbitTemplate.receiveAndConvert(commandQueue, RECEIVE_TIME_OUT, typeRef);
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