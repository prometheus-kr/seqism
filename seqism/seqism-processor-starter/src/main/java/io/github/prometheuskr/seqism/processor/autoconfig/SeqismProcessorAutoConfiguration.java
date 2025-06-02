package io.github.prometheuskr.seqism.processor.autoconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.github.prometheuskr.seqism.processor.config.RabbitConfig;
import io.github.prometheuskr.seqism.processor.endpoint.DefaultSeqismMessageListener;
import io.github.prometheuskr.seqism.processor.helper.ProcessorQueueHelper;

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