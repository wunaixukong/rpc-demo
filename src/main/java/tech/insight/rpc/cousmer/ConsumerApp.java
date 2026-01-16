package tech.insight.rpc.cousmer;

import tech.insight.rpc.api.Add;
import tech.insight.rpc.register.RegisterConfig;
import tech.insight.rpc.register.RegisterServerFactory;
import tech.insight.rpc.register.ServiceRegister;
import tech.insight.rpc.register.ZookeeperServiceRegister;

public class ConsumerApp {
    public static void main(String[] args) throws Exception {

        RegisterConfig registerConfig = new RegisterConfig();
        registerConfig.setRegisterType("zookeeper");
        registerConfig.setConnectString("127.0.0.1:2181");
        ServiceRegister registerServer = RegisterServerFactory.createRegisterServer(registerConfig);
        ConsumerProxyFactory factory = new ConsumerProxyFactory(registerServer);

        Add consumerProxy = factory.createConsumerProxy(Add.class);

        while (true){
            System.out.println(consumerProxy.add(1, 2));
            Thread.sleep(1000);
        }


    }
}
