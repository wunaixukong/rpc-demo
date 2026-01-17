package tech.insight.rpc.provider;

import tech.insight.rpc.api.Add;
import tech.insight.rpc.register.RegistryConfig;

public class ProviderApp {
    public static void main(String[] args) throws Exception {
        RegistryConfig registerConfig = new RegistryConfig();
        registerConfig.setRegisterType("zookeeper");
        registerConfig.setConnectString("127.0.0.1:2181");
        ProviderServerProperty config = new ProviderServerProperty();
        config.setHost("127.0.0.1");
        config.setPort(8887);
        config.setRegisterConfig(registerConfig);
        ProviderServer server = new ProviderServer(config);
        server.register(Add.class, new AddImpl());
        server.start();
    }
}
