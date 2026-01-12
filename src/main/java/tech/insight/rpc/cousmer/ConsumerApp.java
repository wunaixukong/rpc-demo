package tech.insight.rpc.cousmer;

public class ConsumerApp {
    public static void main(String[] args) throws Exception {
        Consumer consumer = new Consumer("localhost", 8888);
        System.out.println(consumer.add(1, 2));
    }
}
