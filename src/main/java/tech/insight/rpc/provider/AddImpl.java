package tech.insight.rpc.provider;

import tech.insight.rpc.api.Add;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class AddImpl implements Add {
    @Override
    public int add(int a, int b) {
        Random random = new Random();
        boolean b1 = random.nextBoolean();
        System.out.println(b1);
        if (b1) {
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(4));
        }
        return a + b;
    }

    @Override
    public int minus(int a, int b) {
        return 0;
    }
}
