package dev.seqism.processor.autoconfig;

import dev.seqism.processor.config.RabbitConfig;
import dev.seqism.processor.endpoint.DefaultSeqismMessageListener;
import dev.seqism.processor.helper.CoreQueueHelper;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    RabbitConfig.class,
    DefaultSeqismMessageListener.class,
    CoreQueueHelper.class
})
public class SeqismProcessorAutoConfiguration {
}