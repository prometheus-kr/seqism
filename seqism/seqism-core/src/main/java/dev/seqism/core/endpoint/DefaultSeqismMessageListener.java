package dev.seqism.core.endpoint;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.core.helper.CoreQueueHelper;
import dev.seqism.core.processor.BizProcessor;
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

    DefaultSeqismMessageListener(CoreQueueHelper queueHelper, ObjectMapper mapper, List<BizProcessor<?>> processors) {
        super(queueHelper);
        this.mapper = mapper;
        this.processorMap = processors.stream().collect(Collectors.toMap(BizProcessor::getBizCode, p -> p));
    }

    @Override
    void proc(SeqismMessage<Object> seqismMessage) {
        String bizCode = seqismMessage.getHeader().getBizCode();
        BizProcessor<?> processor = processorMap.get(bizCode);

        if (processor != null) {
            callProcessor(processor, seqismMessage);
        } else {
            log.error("No processor found for bizCode : [{}]", bizCode);
            queueHelper.sendFinal(new SeqismMessage<>(seqismMessage.getHeader().toFailure(), "Error : No processor found for bizCode[" + bizCode + "]"));
        }
    }

    <T> void callProcessor(BizProcessor<T> processor, SeqismMessage<Object> seqismMessage) {
        processor.process(new SeqismMessage<>(seqismMessage.getHeader(), mapper.convertValue(seqismMessage.getBody(), processor.getBodyType())));
    }
}