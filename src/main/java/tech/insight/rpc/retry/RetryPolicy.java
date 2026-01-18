package tech.insight.rpc.retry;

import tech.insight.rpc.message.Response;

public interface RetryPolicy {

    Response retry(RetryContext retryContext) throws Exception;

}
