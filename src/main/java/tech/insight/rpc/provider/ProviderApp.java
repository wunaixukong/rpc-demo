package tech.insight.rpc.provider;

import tech.insight.rpc.api.Add;
import tech.insight.rpc.register.RegisterConfig;

public class ProviderApp {
    public static void main(String[] args) throws Exception {
        RegisterConfig registerConfig = new RegisterConfig();
        registerConfig.setRegisterType("zookeeper");
        registerConfig.setConnectString("127.0.0.1:2181");
        ProviderServer server = new ProviderServer("127.0.0.1",8888,registerConfig);
        server.register(Add.class, new AddImpl());
        server.start();
    }
}
