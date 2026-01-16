package tech.insight.rpc.register;

public class RegisterServerFactory {

    public static ServiceRegister createRegisterServer(RegisterConfig registerConfig) {
        String registerType = registerConfig.getRegisterType();
        if (registerType.equals("zookeeper")) {
            return new ZookeeperServiceRegister(registerConfig);
        } else if (registerType.equals("redis")) {
            return new RedisServiceRegister();
        }
        throw new UnsupportedOperationException(registerType + "注册中心暂不支持");
    }

}
