package tech.insight.rpc.cousmer;

import lombok.Data;
import tech.insight.rpc.register.RegistryConfig;

@Data
public class ConsumerProperty {

    private Integer workThreadNum = 4;

    private long waitingTime = 3000L;

    private RegistryConfig registerConfig;
}
