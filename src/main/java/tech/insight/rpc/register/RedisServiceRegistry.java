package tech.insight.rpc.register;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RedisServiceRegistry implements ServiceRegistry {
    @Override
    public void init(RegistryConfig config) throws Exception {
        log.warn("redis 注册中心尚未实现");
    }

    @Override
    public void registerServer(ServiceMetaData service) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ServiceMetaData> findServers(String serviceName) {
        throw new UnsupportedOperationException();
    }
}
