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
        property.setWaitingTime(3000L);
        property.setRegisterConfig(registerConfig);
        property.setLoadBalance(new RoundRobinLoadBalance());
        ConsumerProxyFactory factory = new ConsumerProxyFactory(property);
        Add consumerProxy = factory.createConsumerProxy(Add.class);

        while (true){
            try {
                System.out.println(consumerProxy.add(1, 2));
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }


    }
}
