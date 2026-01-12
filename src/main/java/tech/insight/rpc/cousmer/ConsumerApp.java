package tech.insight.rpc.cousmer;

import tech.insight.rpc.api.Add;

public class ConsumerApp {
    public static void main(String[] args) throws Exception {
        Add consumer = new Consumer("localhost", 8888);
        System.out.println(consumer.add(1, 2));
    }
}
