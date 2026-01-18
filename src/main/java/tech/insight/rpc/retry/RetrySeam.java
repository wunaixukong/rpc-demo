package tech.insight.rpc.retry;

import lombok.extern.slf4j.Slf4j;
import tech.insight.rpc.exception.RpcException;
import tech.insight.rpc.message.Response;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class RetrySeam implements RetryPolicy {

    final int retryMax = 3;

    private static final Random random = new Random();

    @Override
    public Response retry(RetryContext retryContext) throws Exception {
        long startTime = System.currentTimeMillis();
        int retryCount = 0;
        while (retryCount < retryMax) {

            long nextDelay = nextDelay(retryCount);
            if (nextDelay > 1000) {
                nextDelay = 1000;
            }
            Thread.sleep(nextDelay);

            long methodTimeoutMs = retryContext.getMethodTimeoutMs() - (System.currentTimeMillis() - startTime);
            log.info("请求失败第{}次重试，剩余时间{},nextDelay:{}", retryCount + 1, methodTimeoutMs,nextDelay);
            if (methodTimeoutMs <= 0L || nextDelay >= methodTimeoutMs) {
                throw new TimeoutException();
            }

            try {
                CompletableFuture<Response> responseFuture = retryContext.doRpc(retryContext.getFailService());

                return responseFuture.get(Math.min(methodTimeoutMs,retryContext.getRequestTimeoutMs()), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("重试出现异常", e);
            }
            retryCount++;
        }

        throw new RpcException("重试失败!");
    }

    private long nextDelay(int retryCount) {
        return 100L * (1 << retryCount) + random.nextInt(0,50);
    }
}
