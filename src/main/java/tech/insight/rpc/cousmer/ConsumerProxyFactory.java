package tech.insight.rpc.cousmer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import lombok.extern.slf4j.Slf4j;
import tech.insight.rpc.api.Add;
import tech.insight.rpc.codec.AlinDecoder;
import tech.insight.rpc.codec.RequestEncoder;
import tech.insight.rpc.exception.RpcException;
import tech.insight.rpc.loadbalance.LoadBalance;
import tech.insight.rpc.loadbalance.RandomLoadBalance;
import tech.insight.rpc.loadbalance.RoundRobinLoadBalance;
import tech.insight.rpc.message.Request;
import tech.insight.rpc.message.Response;
import tech.insight.rpc.register.*;
import tech.insight.rpc.retry.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class ConsumerProxyFactory {
    private final Map<Integer, CompletableFuture<Response>> inFlightRequestMap;

    private final ConnectManage connectManage;

    private final ServiceRegistry serviceRegistry;

    private final ConsumerProperty property;

    private final LoadBalance loadBalance;

    private final RetryPolicy retryPolicy;

    private final HashedWheelTimer timeOutTimer = new HashedWheelTimer(1, TimeUnit.SECONDS, 64);

    public ConsumerProxyFactory(ConsumerProperty property) throws Exception {
        RegistryConfig registerConfig = property.getRegisterConfig();
        serviceRegistry = new DefaultRegistryServer();
        serviceRegistry.init(registerConfig);
        this.property = property;
        this.connectManage = new ConnectManage(createBootstrap(property));
        this.inFlightRequestMap = new ConcurrentHashMap<>();
        this.loadBalance = createLoadBalance();
        this.retryPolicy = createRetryPolicy(property.getRetryPolicy());
    }

    private LoadBalance createLoadBalance() {
        return switch (property.getLoadBalancePolicy()) {
            case "roundRobin" -> new RoundRobinLoadBalance();
            case "random" -> new RandomLoadBalance();
            default -> throw new IllegalArgumentException(property.getLoadBalancePolicy() + "类型不支持");
        };
    }

    private RetryPolicy createRetryPolicy(String retryType) {
        return switch (retryType) {
            case "retrySeam" -> new RetrySeam();
            case "failover" -> new FailoverRetryPolicy();
            case "forking" -> new ForkingRetryPolicy();
            default -> throw new IllegalArgumentException(retryType + "类型不支持");
        };
    }


    @SuppressWarnings("unchecked")
    public <I> I createConsumerProxy(Class<I> interfaceClass) {
        return (I) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{interfaceClass},
                new ConsumerInvocationHandler(interfaceClass));

    }

    private class ConsumerInvocationHandler implements InvocationHandler {
        final Class<?> interfaceClass;

        private ConsumerInvocationHandler(Class<?> interfaceClass) {
            this.interfaceClass = interfaceClass;
        }


        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return invokeObjectMethod(proxy, method, args);
            }

            List<ServiceMetaData> serviceList = serviceRegistry.findServers(interfaceClass.getName());
            if (serviceList.isEmpty()) {
                log.error("在{}中找不到{}", serviceRegistry.getClass(), interfaceClass.getName());
                throw new RpcException(interfaceClass.getName() + "找不到");
            }
            ServiceMetaData provider = loadBalance.select(serviceList);
            Request request = builderRequest(method, args);
            long startTime = System.currentTimeMillis();
            CompletableFuture<Response> requestFuture = callRpcAsync(request, provider);
            long requestTimeoutMs = property.getRequestTimeoutMs();
            Response response;
            try {
                response = requestFuture.get(requestTimeoutMs, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                RetryContext retryContext = new RetryContext();
                long methodTimeoutMs = property.getMethodTimeoutMs() - (System.currentTimeMillis() - startTime);
                if (methodTimeoutMs <= 0 ) {
                    throw new TimeoutException();
                }
                retryContext.setMethodTimeoutMs(methodTimeoutMs);
                retryContext.setRequestTimeoutMs(requestTimeoutMs);
                retryContext.setFailService(provider);
                retryContext.setServiceMetaData(serviceList);
                retryContext.setLoadBalance(loadBalance);
                retryContext.setRequestFunction(providerService -> callRpcAsync(builderRequest(method, args), providerService));
                response = retryPolicy.retry(retryContext);
            }

            return processResponse(response);
        }

        private CompletableFuture<Response> callRpcAsync(Request request, ServiceMetaData provider) {
            CompletableFuture<Response> responseFuture = new CompletableFuture<>();
            Channel channel = connectManage.findChannel(provider.getHost(), provider.getPort());
            if (channel == null) {
                responseFuture.completeExceptionally(new RpcException("provider 连接失败！"));
                return responseFuture;
            }
            inFlightRequestMap.put(request.getRequestId(), responseFuture);

            Timeout timeout = timeOutTimer.newTimeout((t) -> {
                responseFuture.completeExceptionally(new TimeoutException());
            }, property.getRequestTimeoutMs(), TimeUnit.MILLISECONDS);

            responseFuture.whenComplete((r, e) -> {
                inFlightRequestMap.remove(request.getRequestId());
                timeout.cancel();
            });
            channel.writeAndFlush(request).addListener(f -> {
                log.info("请求requestId:{}", request.getRequestId());
                if (!f.isSuccess()) {
                    responseFuture.completeExceptionally(f.cause());
                }
            });
            return responseFuture;
        }

        private Object processResponse(Response response) {
            if (response.getCode() == 200) {
                return response.getResult();
            }
            throw new RpcException(response.getErrorMsg());
        }

        private Request builderRequest(Method method, Object[] args) {
            Request request = new Request();
            request.setMethodName(method.getName());
            request.setServiceName(interfaceClass.getName());
            request.setParameterTypes(method.getParameterTypes());
            request.setParams(args);
            return request;
        }

        private static Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("toString")) {
                return "ALin Proxy Consumer " + Add.class.getName();
            }
            if (method.getName().equals("equals")) {
                return proxy == args[0];
            }
            if (method.getName().equals("hashCode")) {
                return System.identityHashCode(proxy);
            }
            throw new UnsupportedOperationException(method.getName());
        }
    }


    private Bootstrap createBootstrap(ConsumerProperty property) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(property.getWorkThreadNum()))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
//                        ch.pipeline().addLast(new LoggingHandler());
                        ch.pipeline().addLast(new AlinDecoder());
                        ch.pipeline().addLast(new RequestEncoder());
                        ch.pipeline().addLast(new ConsumerChannelHandler());
                    }
                });
        return bootstrap;
    }

    private class ConsumerChannelHandler extends SimpleChannelInboundHandler<Response> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
            CompletableFuture<Response> responseFuture = inFlightRequestMap.remove(response.getRequestId());
            if (responseFuture == null) {
                log.error("requestId{} 找不到", response.getRequestId());
                return;
            }
            responseFuture.complete(response);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("{}已连接", ctx.channel().remoteAddress());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("{}断开连接", ctx.channel().remoteAddress());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("{}出现异常", ctx.channel().remoteAddress(), cause);
        }
    }


}
