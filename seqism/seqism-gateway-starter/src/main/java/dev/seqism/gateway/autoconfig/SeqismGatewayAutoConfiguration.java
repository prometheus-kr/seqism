package dev.seqism.gateway.autoconfig;

import dev.seqism.gateway.config.RabbitConfig;
import dev.seqism.gateway.helper.GateWayQueueHelper;
import dev.seqism.gateway.service.GatewayService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    RabbitConfig.class,
    GateWayQueueHelper.class,
    GatewayService.class
})
public class SeqismGatewayAutoConfiguration {
}