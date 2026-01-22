package tech.insight.rpc.cousmer;

import tech.insight.rpc.api.Add;
import tech.insight.rpc.register.RegistryConfig;

import java.util.concurrent.CyclicBarrier;

public class ConsumerApp {
    public static void main(String[] args) throws Exception {

        RegistryConfig registerConfig = new RegistryConfig();
        registerConfig.setRegisterType("zookeeper");
        registerConfig.setConnectString("127.0.0.1:2181");

        ConsumerProperty property = new ConsumerProperty();
        property.setWorkThreadNum(4);
        property.setRequestTimeoutMs(3000L);
        property.setRegisterConfig(registerConfig);
        ConsumerProxyFactory factory = new ConsumerProxyFactory(property);
        Add consumerProxy = factory.createConsumerProxy(Add.class);

        CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    cyclicBarrier.await();
                    System.out.println(consumerProxy.add(1, 2));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }).start();
        }






    }
}
