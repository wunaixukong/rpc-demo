package tech.insight.rpc.limiter;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 这个的问题就是,
 * permitsPreSeconds = 1000;
 * 0 -->  1
 *     1000 -> 1000
 *  0 --> 0.9 --> 1
 *  那是不是0.1秒2000了?
 */
@Deprecated
public class BucketLimiter implements Limiter {

    private final AtomicInteger tokens;

    private final ScheduledFuture<?> refillSchedule;

    // 没有io操作,所以使用默认
    private static final EventLoop REFILL_EVENT_LOOP = new DefaultEventLoop(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "refill-event-loop");
            thread.setDaemon(true);
            return thread;
        }
    });

    public BucketLimiter(int permitsPreSeconds) {
        this.tokens = new AtomicInteger(permitsPreSeconds);

        this.refillSchedule = REFILL_EVENT_LOOP.scheduleAtFixedRate(() -> tokens.set(permitsPreSeconds),
                1,
                1,
                TimeUnit.SECONDS);
    }

    public void destroy() {
        this.refillSchedule.cancel(false);
    }


    @Override
    public boolean tryAcquire() {
        while (true) {
            int currentTokens = tokens.get();
            if (currentTokens < 0) {
                return false;
            }
            if (tokens.compareAndSet(currentTokens, currentTokens - 1)) {
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
