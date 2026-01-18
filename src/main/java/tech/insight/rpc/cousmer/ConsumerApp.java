package tech.insight.rpc.cousmer;

import tech.insight.rpc.api.Add;
import tech.insight.rpc.loadbalance.RoundRobinLoadBalance;
import tech.insight.rpc.register.RegistryConfig;

public class ConsumerApp {
    public static void main(String[] args) throws Exception {

        RegistryConfig registerConfig = new RegistryConfig();
        registerConfig.setRegisterType("zookeeper");
        registerConfig.setConnectString("127.0.0.1:2181");

        ConsumerProperty property = new ConsumerProperty();
        property.setWorkThreadNum(4);
        property.setRequestTimeoutMs(3000L);
        property.setRegisterConfig(registerConfig);
        property.setLoadBalancePolicy("roundRobin");
        property.setRetryPolicy("retrySeam");
        ConsumerProxyFactory factory = new ConsumerProxyFactory(property);
        Add consumerProxy = factory.createConsumerProxy(Add.class);

        System.out.println(consumerProxy.add(1, 2));


    }
}
