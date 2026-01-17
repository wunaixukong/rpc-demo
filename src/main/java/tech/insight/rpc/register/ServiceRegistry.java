package tech.insight.rpc.register;

import java.util.List;

public interface ServiceRegistry {

    void init(RegistryConfig registryConfig) throws Exception;

    void registerServer(ServiceMetaData service);

    List<ServiceMetaData> findServers(String serviceName) throws Exception;

}
