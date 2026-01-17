package tech.insight.rpc.register;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultRegistryServer implements ServiceRegistry {

    private ServiceRegistry delegate;

    private final Map<String,List<ServiceMetaData>> CACHE = new ConcurrentHashMap<>();

    @Override
    public void init(RegistryConfig config) throws Exception {
        this.delegate = createRegistryServer(config);
        this.delegate.init(config);
    }

    @Override
    public void registerServer(ServiceMetaData service) {
        delegate.registerServer(service);
    }

    @Override
    public List<ServiceMetaData> findServers(String serviceName) {
        try {
            List<ServiceMetaData> servers = delegate.findServers(serviceName);
            CACHE.put(serviceName, servers);
            return servers;
        }catch (Exception e){
            log.error("{} 注册中心找不到{}服务",delegate.getClass(),serviceName,e);
            return CACHE.getOrDefault(serviceName,List.of());
        }
    }

    private ServiceRegistry createRegistryServer(RegistryConfig registerConfig) {
        String registerType = registerConfig.getRegisterType();
        if (registerType.equals("zookeeper")) {
            return new ZookeeperServiceRegistry();
        } else if (registerType.equals("redis")) {
            return new RedisServiceRegistry();
        }
        throw new UnsupportedOperationException(registerType + "注册中心暂不支持");
    }
}
