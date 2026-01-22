package tech.insight.rpc.cousmer;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import lombok.extern.slf4j.Slf4j;
import tech.insight.rpc.exception.LimitException;
import tech.insight.rpc.limiter.ConcurrencyLimiter;
import tech.insight.rpc.limiter.Limiter;
import tech.insight.rpc.limiter.RateLimiter;
import tech.insight.rpc.message.Request;
import tech.insight.rpc.message.Response;
import tech.insight.rpc.register.ServiceMetaData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class InFlightRequestManage {

    private final Map<Integer, CompletableFuture<Response>> inFlightRequestMap;

    private final HashedWheelTimer timeOutTimer ;

    private final Limiter globalLimiter;

    private final Map<ServiceMetaData,Limiter> channelLimiterMap;

    private final ConsumerProperty property;

    public InFlightRequestManage(ConsumerProperty property)  {
        this.inFlightRequestMap = new HashMap<>();
        this.globalLimiter = new ConcurrencyLimiter(property.getRpcPerSecond());
        this.timeOutTimer = new HashedWheelTimer(1, TimeUnit.SECONDS, 64);
        this.channelLimiterMap = new ConcurrentHashMap<>();
        this.property = property;
    }



    public CompletableFuture<Response> inFlightRequest(Request request, long timeoutMs,ServiceMetaData serviceMetaData) {
        CompletableFuture<Response> response = new CompletableFuture<>();
        if (!globalLimiter.tryAcquire()) {
            response.completeExceptionally(new LimitException("全局限流,当前在途请求超过阈值"));
            return response;
        }
        Limiter channelLimiter = channelLimiterMap.computeIfAbsent(serviceMetaData,
                v -> new RateLimiter(property.getRpcPerChannel()));

        if (!channelLimiter.tryAcquire()) {
            response.completeExceptionally(new LimitException("Channel限流,当前在途请求超过阈值"));
            return response;
        }
        inFlightRequestMap.put(request.getRequestId(), response);

        Timeout timeout = timeOutTimer.newTimeout((t) -> {
            response.completeExceptionally(new TimeoutException());
        }, timeoutMs, TimeUnit.MILLISECONDS);

        response.whenComplete((r, e) -> {
            inFlightRequestMap.remove(request.getRequestId());
            globalLimiter.release();
            channelLimiter.release();
            timeout.cancel();
        });

        return response;
    }

    public boolean completeRequest(Integer requestId,Response response) {
        CompletableFuture<Response> future = inFlightRequestMap.get(requestId);
        if (future == null) {
            log.warn("requestId:{} 空闲返回", requestId);
            return false;
        }
        return future.complete(response);
    }

    public boolean completeExceptionRequest(Integer requestId,Exception e) {
        CompletableFuture<Response> future = inFlightRequestMap.get(requestId);
        if (future == null) {
            log.warn("requestId:{} 空闲异常", requestId,e);
            return false;
        }
        return future.completeExceptionally(e);
    }


}
