package dev.seqism.gateway.autoconfig;

import dev.seqism.gateway.config.RabbitConfig;
import dev.seqism.gateway.helper.GateWayQueueHelper;
import dev.seqism.gateway.service.GatewayService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration class for the Seqism Gateway module.
 * <p>
 * This configuration class imports and initializes the following components:
 * <ul>
 * <li>{@link RabbitConfig} - Configuration for RabbitMQ integration.</li>
 * <li>{@link GateWayQueueHelper} - Helper utilities for gateway queue operations.</li>
 * <li>{@link GatewayService} - Core service for gateway functionality.</li>
 * </ul>
 * <p>
 * This class is automatically detected and applied by Spring Boot's auto-configuration mechanism.
 */
@Configuration
@Import({
        RabbitConfig.class,
        GateWayQueueHelper.class,
        GatewayService.class
})
public class SeqismGatewayAutoConfiguration {}