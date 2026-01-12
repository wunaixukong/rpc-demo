package tech.insight.rpc.provider;

import tech.insight.rpc.api.Add;

public class ProviderApp {
    public static void main(String[] args) throws IllegalAccessException {
        ProviderServer server = new ProviderServer(8888);
        server.register(Add.class, new AddImpl());
        server.start();
    }
}
