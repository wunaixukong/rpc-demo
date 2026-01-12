package tech.insight.rpc.message;

import lombok.Data;

@Data
public class Request {
    private String serviceName;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] params;


}
