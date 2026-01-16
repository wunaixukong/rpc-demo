package tech.insight.rpc.register;

import java.util.List;

public interface ServiceRegister {

    void init() throws Exception;

    void registerServer(ServiceMetaData service);

    List<ServiceMetaData> findServers(String serviceName);

}
