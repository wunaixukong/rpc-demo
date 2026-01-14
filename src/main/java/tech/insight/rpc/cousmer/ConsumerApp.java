package tech.insight.rpc.cousmer;

import tech.insight.rpc.api.Add;

public class ConsumerApp {
    public static void main(String[] args) throws Exception {

        ConsumerProxyFactory factory = new ConsumerProxyFactory();
        for (int i = 0; i < 10; i++) {
            Add consumerProxy = factory.createConsumerProxy(Add.class);


            System.out.println(consumerProxy.add(1, 2));
            System.out.println(consumerProxy.add(10, 2));
            System.out.println(consumerProxy.add(13, 2));
        }

    }
}
