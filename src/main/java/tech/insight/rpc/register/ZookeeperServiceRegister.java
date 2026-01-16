package tech.insight.rpc.register;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.util.List;

@Slf4j
public class ZookeeperServiceRegister implements ServiceRegister {

    private CuratorFramework client;

    private final RegisterConfig registerConfig;

    private final static String BAST_PATH = "/alin-service";

    private ServiceDiscovery<ServiceMetaData> serviceDiscovery;


    public ZookeeperServiceRegister(RegisterConfig registerConfig) {
        this.registerConfig = registerConfig;
    }

    @Override
    public void init() throws Exception {
        client = CuratorFrameworkFactory.builder().
                connectString(registerConfig.getConnectString())
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(3000)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();

        serviceDiscovery = ServiceDiscoveryBuilder
                .builder(ServiceMetaData.class)
                .basePath(BAST_PATH)
                .client(client)
                .serializer(new JsonInstanceSerializer<>(ServiceMetaData.class))
                .build();

        serviceDiscovery.start();
        log.info("zookeeper init success");
    }

    @Override
    public void registerServer(ServiceMetaData service) {
        try {
            ServiceInstance<ServiceMetaData> serviceInstance = ServiceInstance.<ServiceMetaData>builder()
                    .name(service.getServiceName())
                    .port(service.getPort())
                    .address(service.getHost())
                    .payload(service)
                    .build();
            serviceDiscovery.registerService(serviceInstance);
        }catch (Exception e){
            log.error("{}注册出现异常", service.getServiceName(), e);
        }

    }

    @Override
    public List<ServiceMetaData> findServers(String serviceName) {
        try {
            return serviceDiscovery.queryForInstances(serviceName)
                    .stream().map(this::builderMetaData).toList();
        } catch (Exception e) {
            log.error("没有找到provider:{}",serviceName, e);
        }
        return List.of();
    }

    private ServiceMetaData builderMetaData(ServiceInstance<ServiceMetaData> serviceInstance) {
        return serviceInstance.getPayload();
    }
}
