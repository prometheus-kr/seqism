package dev.seqism.gateway.helper;

import dev.seqism.common.constant.SeqismConstant;
import dev.seqism.common.helper.QueueNameHelper;
import dev.seqism.common.vo.ErrorInfo;
import dev.seqism.common.vo.SeqismException;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GateWayQueueHelper {
    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final ParameterizedTypeReference<SeqismMessage<Object>> typeRef = new ParameterizedTypeReference<SeqismMessage<Object>>() {};

    public GateWayQueueHelper(RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    public SeqismMessage<Object> sendAndReceiveInit(SeqismMessage<Object> message) {
        log.debug("Sending message : [{}]", message);

        createQueues(message);

        send(QueueNameHelper.getStaticQueueName(), message);
        return receive(message);
    }

    public SeqismMessage<Object> sendAndReceiveNext(SeqismMessage<Object> message) {
        log.debug("Sending message : [{}]", message);
        String tranId = message.getHeader().getTranId();

        send(QueueNameHelper.getResponseQueueName(tranId), message);
        return receive(message);
    }

    void createQueues(SeqismMessage<Object> message) {
        String tranId = message.getHeader().getTranId();

        String commandQueue = QueueNameHelper.getCommandQueueName(tranId);
        String responseQueue = QueueNameHelper.getResponseQueueName(tranId);

        declareQueue(commandQueue);
        declareQueue(responseQueue);
        log.debug("Created queues : [{}], [{}]", commandQueue, responseQueue);
    }

    void declareQueue(String queueName) {
        Queue queue = QueueBuilder.durable(queueName)
                .withArgument("x-expires", SeqismConstant.QUEUE_DELETE_TIME)
                .build();
        rabbitAdmin.declareQueue(queue);
    }

    void send(String queueName, SeqismMessage<Object> message) {
        try {
            if (rabbitAdmin.getQueueProperties(queueName) == null) {
                throw new SeqismException(ErrorInfo.ERROR_0001_0003);
            }

            rabbitTemplate.convertAndSend(queueName, message);
        } catch (AmqpException e) {
            throw new SeqismException(ErrorInfo.ERROR_0001_0004, e);
        }
    }

    SeqismMessage<Object> receive(SeqismMessage<Object> message) {
        String tranId = message.getHeader().getTranId();

        String commandQueue = QueueNameHelper.getCommandQueueName(tranId);
        String responseQueue = QueueNameHelper.getResponseQueueName(tranId);

        try {
            SeqismMessage<Object> receivedMsg = rabbitTemplate.receiveAndConvert(commandQueue,
                    SeqismConstant.RECEIVE_TIME_OUT, typeRef);
            log.debug("Received message : [{}]", receivedMsg);

            // 타임아웃 동안 응답을 받지 못하거나, 성공/실패 상태인 경우 큐 삭제
            if (receivedMsg == null || receivedMsg.getHeader().getStatus() != SeqismMessageStatus.IN_PROGRESS) {
                rabbitAdmin.deleteQueue(commandQueue);
                rabbitAdmin.deleteQueue(responseQueue);
                log.debug("Deleted queues : [{}], [{}]", commandQueue, responseQueue);
            }

            return receivedMsg;
        } catch (AmqpException e) {
            throw new SeqismException(ErrorInfo.ERROR_0001_0005, e);
        }
    }
}