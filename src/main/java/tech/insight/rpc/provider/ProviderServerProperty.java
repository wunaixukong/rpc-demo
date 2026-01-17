package tech.insight.rpc.provider;

import lombok.Data;
import tech.insight.rpc.register.RegistryConfig;

@Data
public class ProviderServerProperty {
    private String host;
    private int port;
    private Integer workThreadNum = 4;

    private RegistryConfig registerConfig;
}
