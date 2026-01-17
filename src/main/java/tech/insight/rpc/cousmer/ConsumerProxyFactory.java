package tech.insight.rpc.cousmer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import tech.insight.rpc.api.Add;
import tech.insight.rpc.codec.AlinDecoder;
import tech.insight.rpc.codec.RequestEncoder;
import tech.insight.rpc.exception.RpcException;
import tech.insight.rpc.message.Request;
import tech.insight.rpc.message.Response;
import tech.insight.rpc.register.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class ConsumerProxyFactory {
    private final Map<Integer, CompletableFuture<Response>> inFlightRequestMap;

    private final ConnectManage connectManage ;

    private final ServiceRegistry serviceRegistry;

    private final ConsumerProperty property;

    public ConsumerProxyFactory(ConsumerProperty property) throws Exception {
        RegistryConfig registerConfig = property.getRegisterConfig();
        serviceRegistry = new DefaultRegistryServer();
        serviceRegistry.init(registerConfig);
        this.property = property;
        this.connectManage =  new ConnectManage(createBootstrap(property));
        this.inFlightRequestMap = new ConcurrentHashMap<>();
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

            try {
                CompletableFuture<Response> completableFuture = new CompletableFuture<>();
                List<ServiceMetaData> servers = serviceRegistry.findServers(interfaceClass.getName());
                if (servers.isEmpty()) {
                    log.error("{}服务找不到", interfaceClass.getName());
                    throw new RpcException(interfaceClass.getName() + "找不到");
                }
                ServiceMetaData metaData = servers.get(0);
                Channel channel = connectManage.findChannel(metaData.getHost(), metaData.getPort());
                Request request = builderRequest(method, args);
                inFlightRequestMap.put(request.getRequestId(), completableFuture);
                channel.writeAndFlush(request).addListener(f -> {
                    if (!f.isSuccess()) {
                        inFlightRequestMap.remove(request.getRequestId());
                        completableFuture.completeExceptionally(f.cause());
                    }
                });
                return processResponse(completableFuture);
            } catch (RpcException rpcException) {
                throw rpcException;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Object processResponse(CompletableFuture<Response> completableFuture) throws InterruptedException, ExecutionException, TimeoutException {
            Response response = completableFuture.get(property.getWaitingTime(), TimeUnit.MILLISECONDS);
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
                log.error("{}requestId 找不到", response.getRequestId());
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
