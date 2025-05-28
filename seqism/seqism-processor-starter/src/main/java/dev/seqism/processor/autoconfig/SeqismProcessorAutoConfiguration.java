package dev.seqism.processor.autoconfig;

import dev.seqism.processor.config.RabbitConfig;
import dev.seqism.processor.endpoint.DefaultSeqismMessageListener;
import dev.seqism.processor.helper.ProcessorQueueHelper;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration class for the Seqism Processor module.
 * <p>
 * This configuration class imports and initializes the following components:
 * <ul>
 * <li>{@link RabbitConfig} - Configuration for RabbitMQ integration.</li>
 * <li>{@link DefaultSeqismMessageListener} - Registers the default message listener for processing messages.</li>
 * <li>{@link ProcessorQueueHelper} - Provides helper methods for queue management.</li>
 * </ul>
 * <p>
 * This class is automatically detected and applied by Spring Boot's auto-configuration mechanism.
 */
@Configuration
@Import({
        RabbitConfig.class,
        DefaultSeqismMessageListener.class,
        ProcessorQueueHelper.class
})
public class SeqismProcessorAutoConfiguration {
    /**
     * Default constructor for the {@code SeqismProcessorAutoConfiguration} class.
     * Initializes a new instance of the configuration without any parameters.
     */
    public SeqismProcessorAutoConfiguration() {}
}