package dev.seqism.core;

import dev.seqism.common.vo.SeqismMessage;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageBody;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageHeader;
import dev.seqism.common.vo.SeqismMessage.SeqismMessageStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DefaultMessageListener
 *
 * @author seqism
 * @since 2025.05.16
 */
@Slf4j
@Component
public class DefaultSeqismMessageListener extends SeqismMessageListener {
    private final Map<String, BizProcessor> processorMap;

    DefaultSeqismMessageListener(CoreQueueHelper queueHelper, List<BizProcessor> processors) {
        super(queueHelper);
        this.processorMap = processors.stream().collect(Collectors.toMap(BizProcessor::getBizCode, p -> p));
    }

    @Override
    void proc(SeqismMessage seqismMessage) {
        String bizCode = seqismMessage.getHeader().getBizCode();
        BizProcessor processor = processorMap.get(bizCode);

        if (processor != null) {
            processor.process(seqismMessage, queueHelper);
        } else {
            log.error("No processor found for bizCode : [{}]", bizCode);
            queueHelper.sendFinal(new SeqismMessage(seqismMessage.getHeader().toFailure(), new SeqismMessageBody("No processor found for bizCode : " + bizCode)));
        }
    }
}