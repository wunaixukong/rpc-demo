package tech.insight.rpc.cousmer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import tech.insight.rpc.api.Add;
import tech.insight.rpc.codec.AlinDecoder;
import tech.insight.rpc.codec.ResponseEncoder;
import tech.insight.rpc.exception.RpcException;
import tech.insight.rpc.message.Request;
import tech.insight.rpc.codec.RequestEncoder;
import tech.insight.rpc.message.Response;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;


public class Consumer implements Add {

    private final String host;
    private final int port;

    public Consumer(String host, int port){
        this.host = host;
        this.port = port;
    }

    @Override
    public int add(int a, int b)  {
        try {
            Bootstrap bootstrap = new Bootstrap();
            CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
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
                                protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
                                    if (msg.getCode() == 200) {
                                        completableFuture.complete(Integer.parseInt(msg.getResult().toString()));
                                    }else {
                                        completableFuture.completeExceptionally(new RpcException(msg.getErrorMsg()));
                                    }

                                }
                            });
                        }
                    });
            ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            Request request = new Request();
            request.setMethodName("add");
            request.setServiceName(Add.class.getCanonicalName());
            request.setParameterTypes(new Class[]{int.class, int.class});
            request.setParams(new Object[]{a, b});
            connectFuture.channel().writeAndFlush(request);
            return completableFuture.get();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
