package tech.insight.rpc.limiter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class RateLimiter implements Limiter {

    // 当前允许执行的纳秒时间,
    // intervalNs = 5000;
    // nextTokens = 500000;
    // 表示: 在 [500000,+∞) 之间的数据可以执行 1次请求
    // 如果执行之后,下一个执行区间是 [505000,+∞)
// nextTokens 表示下一次请求允许执行的最早纳秒时间点。
// 每次请求成功后，会将该时间点向后推移一个 intervalNs，确保两次请求间隔至少为 intervalNs。
    private final AtomicLong nextTokens = new AtomicLong(0L);

    private final long intervalNs;

    private final long waitingNs;


    public RateLimiter(int permitsPreSeconds) {
        this.intervalNs = TimeUnit.SECONDS.toNanos(1) / permitsPreSeconds;
        this.waitingNs = TimeUnit.MILLISECONDS.toNanos(500);
    }


    @Override
    public boolean tryAcquire() {
        while (true) {
            long next = System.nanoTime();
            long pre = nextTokens.get();
            if (next + waitingNs < pre) {
                return false;
            }
            if (nextTokens.compareAndSet(pre, next + intervalNs)) {
                return true;
            }
        }
    }

    @Override
    public void release() {
        // 我是傻逼,难道你还想一个个加?
//        tokens.compareAndSet();
    }
}
