package tech.insight.rpc.loadbalance;

import tech.insight.rpc.register.ServiceMetaData;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalance implements LoadBalance{
    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public ServiceMetaData select(List<ServiceMetaData> list) {
        return list.get(Math.abs(index.incrementAndGet()) % list.size());
    }
}
