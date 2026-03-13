package io.github.prometheuskr.seqism.ex.processor.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.github.prometheuskr.seqism.common.vo.SeqismMessage;
import io.github.prometheuskr.seqism.processor.SeqismProcessor;
import io.github.prometheuskr.seqism.processor.helper.ProcessorQueueHelper;

@Slf4j
@Component
public class OnetimeSample003
        extends SeqismProcessor<OnetimeSample003.OrderBody, OnetimeSample003.OrderBody> {

    public OnetimeSample003(ObjectMapper mapper, ProcessorQueueHelper queueHelper) {
        super(mapper, queueHelper);
    }

    @Override
    public String getBizCode() {
        return "Onetime003";
    }

    @Override
    public Class<OrderBody> getBodyType() {
        return OrderBody.class;
    }

    @Override
    public void process(SeqismMessage<OrderBody> message) {
        throw new UnsupportedOperationException("process is not supported for Onetime003");
    }

    @Override
    public SeqismMessage<?> processOnetime(SeqismMessage<OrderBody> message) {
        OrderBody body = message.getBody();

        List<OrderItem> processedItems = new ArrayList<>();
        int totalAmount = 0;
        for (OrderItem item : body.getItems()) {
            int itemTotal = item.getPrice() * item.getQuantity();
            totalAmount += itemTotal;
            processedItems.add(new OrderItem(item.getItemName(), item.getPrice(), item.getQuantity(), itemTotal));
        }

        OrderBody result = new OrderBody(body.getOrderId(), processedItems, totalAmount, "CONFIRMED");
        return message.withBody(result).toSuccess();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBody implements Serializable {
        private String orderId;
        private List<OrderItem> items;
        private int totalAmount;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem implements Serializable {
        private String itemName;
        private int price;
        private int quantity;
        private int subtotal;
    }
}
