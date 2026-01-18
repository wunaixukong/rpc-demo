package tech.insight.rpc.retry;

import lombok.extern.slf4j.Slf4j;
import tech.insight.rpc.exception.RpcException;
import tech.insight.rpc.loadbalance.LoadBalance;
import tech.insight.rpc.message.Response;
import tech.insight.rpc.register.ServiceMetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ForkingRetryPolicy implements RetryPolicy{

    @Override
    public Response retry(RetryContext retryContext) throws Exception {
        ServiceMetaData failService = retryContext.getFailService();
        List<ServiceMetaData> serviceMetaDataList = retryContext.getServiceMetaData();
        List<ServiceMetaData> serviceList = new ArrayList<>(serviceMetaDataList);
        serviceList.remove(failService);
        if (serviceList.isEmpty()){
            throw new RpcException("没有可重试的provider");
        }

        CompletableFuture[] allFuture = new CompletableFuture[serviceList.size()];
        for (int i = 0; i < serviceList.size(); i++) {
            allFuture[i] = retryContext.doRpc(serviceList.get(i));
        }
        CompletableFuture<Object> mainFuture = CompletableFuture.anyOf(allFuture);
        return (Response) mainFuture.get(Math.min(retryContext.getMethodTimeoutMs(),retryContext.getRequestTimeoutMs()), TimeUnit.MILLISECONDS);
    }
}
