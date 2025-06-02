package io.github.prometheuskr.seqism.ex.processor.example;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.prometheuskr.seqism.common.vo.SeqismMessage;
import io.github.prometheuskr.seqism.processor.SeqismProcessor;
import io.github.prometheuskr.seqism.processor.helper.ProcessorQueueHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class SeqismProcessorSample003
        extends SeqismProcessor<SeqismProcessorSample003.Sample003Body, SeqismProcessorSample003.Sample003Body> {

    public SeqismProcessorSample003(ObjectMapper mapper, ProcessorQueueHelper queueHelper) {
        super(mapper, queueHelper);
    }

    @Override
    public String getBizCode() {
        return "Sample003";
    }

    @Override
    public Class<Sample003Body> getBodyType() {
        return Sample003Body.class;
    }

    @Override
    public void process(SeqismMessage<Sample003Body> message) {
        Sample003Body body = message.getBody();

        // Step 1: Add a new transaction
        body.getTransactions().add(new Transaction("TXN-001", 1000, LocalDateTime.now(), "INIT"));
        SeqismMessage<Sample003Body> response = sendAndReceiveOrThrow(message.withBody(body));

        // Step 2: Update status of last transaction
        Sample003Body respBody = response.getBody();
        Transaction lastTxn = respBody.getTransactions().get(respBody.getTransactions().size() - 1);
        lastTxn.setStatus("STEP2");
        response = sendAndReceiveOrThrow(response.withBody(respBody));

        // Step 3: Add another transaction
        respBody = response.getBody();
        respBody.getTransactions().add(new Transaction("TXN-002", 2000, LocalDateTime.now(), "STEP3"));
        response = sendAndReceiveOrThrow(response.withBody(respBody));

        // Step 4: Mark all as completed
        respBody = response.getBody();
        for (Transaction txn : respBody.getTransactions()) {
            txn.setStatus("DONE");
        }
        sendFinal(response.withBody(respBody).toSuccess());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sample003Body implements Serializable {
        private String userId;
        private List<Transaction> transactions;
        private Meta meta;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Transaction implements Serializable {
        private String txnId;
        private int amount;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime timestamp;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta implements Serializable {
        private String requestIp;
        private String deviceType;
    }
}