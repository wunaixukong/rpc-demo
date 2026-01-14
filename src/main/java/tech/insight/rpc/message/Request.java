package tech.insight.rpc.message;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class Request {

    private AtomicInteger atomicInteger = new AtomicInteger();

    private Integer requestId = atomicInteger.incrementAndGet();

    private String serviceName;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] params;


}
