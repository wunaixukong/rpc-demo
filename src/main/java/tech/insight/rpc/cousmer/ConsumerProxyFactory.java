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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ConsumerProxyFactory {
    private final Map<Integer, CompletableFuture<Response>> inFlightRequestMap = new ConcurrentHashMap<>();

    private final ConnectManage connectManage = new ConnectManage(createBootstrap());


    @SuppressWarnings("unchecked")
    public <I> I createConsumerProxy(Class<I> classServer){
        return (I) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{classServer}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass() == Object.class) {
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


                try {
                    Channel channel = connectManage.findChannel("localhost", 8888);
                    CompletableFuture<Response> completableFuture = new CompletableFuture<>();
                    Request request = new Request();
                    request.setMethodName(method.getName());
                    request.setServiceName(Add.class.getCanonicalName());
                    request.setParameterTypes(method.getParameterTypes());
                    request.setParams(args);
                    inFlightRequestMap.put(request.getRequestId(), completableFuture);
                    channel.writeAndFlush(request);
                    Response response = completableFuture.get(3, TimeUnit.SECONDS);
                    if (response.getCode() == 200) {
                        return response.getResult();
                    }
                    throw new RpcException(response.getErrorMsg());
                } catch (RpcException rpcException) {
                    throw rpcException;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }


    private Bootstrap createBootstrap() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(4))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
//                        ch.pipeline().addLast(new LoggingHandler());
                        ch.pipeline().addLast(new AlinDecoder());
                        ch.pipeline().addLast(new RequestEncoder());
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<Response>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
                                CompletableFuture<Response> responseFuture = inFlightRequestMap.remove(response.getRequestId());
                                if (responseFuture == null) {
                                    log.error("{}requestId 找不到", response.getRequestId());
                                    return;
                                }
                                responseFuture.complete(response);
                            }
                        });
                    }
                });
        return bootstrap;
    }


}
