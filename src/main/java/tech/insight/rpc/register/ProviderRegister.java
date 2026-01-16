package tech.insight.rpc.register;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderRegister {

    // 这个地方的泛型应该怎么加呢
    private final Map<String, ServerInstanceWrapper> serverInstanceMap = new ConcurrentHashMap<>();

    public <I> void register(Class<I> serverInterface,I serverInstance) throws IllegalAccessException {
        if (!serverInterface.isInterface()) {
            throw new IllegalAccessException("注册的服务必须是一个接口" + serverInterface.getSimpleName());
        }
        if (serverInstanceMap.putIfAbsent(serverInterface.getName(),new ServerInstanceWrapper<>(serverInstance)) != null) {
            throw new IllegalAccessException("不能重复注册" + serverInterface.getSimpleName());
        }
    }


    public ServerInstanceWrapper findServer(String serviceName) {
        return serverInstanceMap.get(serviceName);
    }

    public List<String> allServerName(){
        return new ArrayList<>(serverInstanceMap.keySet());
    }

    public static class ServerInstanceWrapper<I> {
        final I serverInstance;

        public ServerInstanceWrapper(I serverInstance) {
            this.serverInstance = serverInstance;
        }

        public Object invoke(String methodName, Class<?>[] parameterTypes, Object[] params) throws Exception {
            Method invokeMethod = serverInstance.getClass().getDeclaredMethod(methodName, parameterTypes);
            return invokeMethod.invoke(serverInstance,params);
        }

    }
}
