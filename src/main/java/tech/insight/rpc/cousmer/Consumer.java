package tech.insight.rpc.cousmer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import tech.insight.rpc.api.Add;
import tech.insight.rpc.codec.AlinDecoder;
import tech.insight.rpc.codec.RequestEncoder;
import tech.insight.rpc.exception.RpcException;
import tech.insight.rpc.message.Request;
import tech.insight.rpc.message.Response;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Slf4j
public class Consumer implements Add {

    private final String host;
    private final int port;

    private final Map<Integer, CompletableFuture<Response>> inFlightRequestMap = new ConcurrentHashMap<>();

    private final ConnectManage connectManage = new ConnectManage(createBootstrap());

    public Consumer(String host, int port) {
        this.host = host;
        this.port = port;
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

    @Override
    public int add(int a, int b) {
        try {
            Channel channel = connectManage.findChannel(host, port);
            CompletableFuture<Response> completableFuture = new CompletableFuture<>();
            Request request = new Request();
            request.setMethodName("add");
            request.setServiceName(Add.class.getCanonicalName());
            request.setParameterTypes(new Class[]{int.class, int.class});
            request.setParams(new Object[]{a, b});
            inFlightRequestMap.put(request.getRequestId(), completableFuture);
            channel.writeAndFlush(request);
            Response response = completableFuture.get(3, TimeUnit.SECONDS);
            if (response.getCode() == 200) {
                return (int) response.getResult();
            }
            throw new RpcException(response.getErrorMsg());
        } catch (RpcException rpcException) {
            throw rpcException;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int minus(int a, int b) {
        return 0;
    }
}
