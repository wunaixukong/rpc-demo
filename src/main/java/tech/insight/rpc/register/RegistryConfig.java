package tech.insight.rpc.register;

import lombok.Data;

@Data
public class RegistryConfig {
    private String registerType;

    private String connectString;

}
