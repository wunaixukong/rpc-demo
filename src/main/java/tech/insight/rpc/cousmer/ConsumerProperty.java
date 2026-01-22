package tech.insight.rpc.cousmer;

import lombok.Data;
import tech.insight.rpc.register.RegistryConfig;

@Data
public class ConsumerProperty {

    private Integer workThreadNum = 4;

    private long requestTimeoutMs = 3000L;

    private long methodTimeoutMs = 10000L;

    private RegistryConfig registerConfig;

    private String loadBalancePolicy = "roundRobin";

    private String retryPolicy = "forking";

    private int rpcPerSecond = 10;

    private int rpcPerChannel = 5;
}
