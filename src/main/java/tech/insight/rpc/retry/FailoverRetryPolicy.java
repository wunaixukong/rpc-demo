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
public class FailoverRetryPolicy implements RetryPolicy{

    @Override
    public Response retry(RetryContext retryContext) throws Exception {
        LoadBalance loadBalance = retryContext.getLoadBalance();
        ServiceMetaData failService = retryContext.getFailService();
        List<ServiceMetaData> serviceMetaDataList = retryContext.getServiceMetaData();
        log.info("serviceMetaData={}, failService={}",serviceMetaDataList,failService);
        List<ServiceMetaData> serviceList = new ArrayList<>(serviceMetaDataList);
        serviceList.remove(failService);
        if (serviceList.isEmpty()){
            throw new RpcException("没有可重试的provider");
        }
        ServiceMetaData selectService = loadBalance.select(serviceList);
        CompletableFuture<Response> future = retryContext.doRpc(selectService);
        log.info("selectService={}",selectService);

        return future.get(Math.min(retryContext.getMethodTimeoutMs(),retryContext.getRequestTimeoutMs()), TimeUnit.MILLISECONDS);
    }
}
