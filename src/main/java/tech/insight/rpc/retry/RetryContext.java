package tech.insight.rpc.retry;

import lombok.Data;
import tech.insight.rpc.loadbalance.LoadBalance;
import tech.insight.rpc.message.Response;
import tech.insight.rpc.register.ServiceMetaData;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Data
public class RetryContext {

    private ServiceMetaData failService;

    private long methodTimeoutMs;

    private long requestTimeoutMs;

    private LoadBalance loadBalance;

    private List<ServiceMetaData> serviceMetaData;

    private CompletableFuture<Response> completableFuture;

    private Function<ServiceMetaData,CompletableFuture<Response>> requestFunction;

    public CompletableFuture<Response> doRpc(ServiceMetaData serviceMetaData)  {
        return requestFunction.apply(serviceMetaData);
    }

}
