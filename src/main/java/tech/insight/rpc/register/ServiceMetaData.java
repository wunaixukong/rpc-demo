package tech.insight.rpc.register;

import lombok.Data;

@Data
public class ServiceMetaData {

    private String serviceName;

    private String host;

    private int port;


}
