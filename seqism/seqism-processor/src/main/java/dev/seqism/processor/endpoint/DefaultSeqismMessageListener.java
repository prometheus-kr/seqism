package dev.seqism.processor.endpoint;

import dev.seqism.common.vo.ErrorInfo;
import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.processor.helper.CoreQueueHelper;
import dev.seqism.processor.BizProcessor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * DefaultMessageListener
 *
 * @author seqism
 * @since 2025.05.16
 */
@Slf4j
@Component
public class DefaultSeqismMessageListener extends SeqismMessageListener<Object> {
    private final ObjectMapper mapper;
    private final Map<String, BizProcessor<?>> processorMap;

    DefaultSeqismMessageListener(ObjectMapper mapper, CoreQueueHelper queueHelper, List<BizProcessor<?>> processors) {
        super(queueHelper);
        this.mapper = mapper;
        this.processorMap = processors.stream().collect(Collectors.toMap(BizProcessor::getBizCode, p -> p));
    }

    @Override
    void proc(SeqismMessage<Object> message) {
        String bizCode = message.getHeader().getBizCode();
        BizProcessor<?> processor = processorMap.get(bizCode);

        if (processor != null) {
            callProcessor(processor, message);
        } else {
            log.error("No processor found for bizCode : [{}]", bizCode);
            queueHelper.sendFinal(message.toFailure(ErrorInfo.ERROR_0002_0001, bizCode));
        }
    }

    <T> void callProcessor(BizProcessor<T> processor, SeqismMessage<Object> message) {
        processor.process(message.withBody(mapper.convertValue(message.getBody(), processor.getBodyType())));
    }
}