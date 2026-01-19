package tech.insight.rpc.limiter;

/**
 * 并发限流 : 100,最多允许100个请求
 * 速率限流 : 20/s,每秒最多20个请求
 */
public interface Limiter {

    boolean tryAcquire();

    void release();

}
