package tech.insight.rpc.loadbalance;

import tech.insight.rpc.register.ServiceMetaData;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomLoadBalance implements LoadBalance{
    private final Random random = new Random();

    @Override
    public ServiceMetaData select(List<ServiceMetaData> list) {
        int index = random.nextInt(0,list.size()) ;
        return list.get(index);
    }
}
