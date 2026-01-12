package tech.insight.rpc.provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import tech.insight.rpc.message.Request;
import tech.insight.rpc.message.Response;
import tech.insight.rpc.codec.AlinDecoder;
import tech.insight.rpc.codec.ResponseEncoder;
import tech.insight.rpc.register.ProviderRegister;

public class ProviderServer {

    private final int port;

    private  EventLoopGroup bossGroup;

    private  EventLoopGroup workerGroup;

    private ProviderRegister register;

    public ProviderServer(int port) {
        this.port = port;
        this.register = new ProviderRegister();
    }

    public void start(){
        try {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup(4);
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
//                            ch.pipeline().addLast(new LoggingHandler());
                            ch.pipeline().addLast(new AlinDecoder());
                            ch.pipeline().addLast(new ResponseEncoder());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<Request>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
                                    ProviderRegister.ServerInstanceWrapper<?> server = register.findServer(request.getServiceName());
                                    Object result = server.invoke(request.getMethodName(),request.getParameterTypes(),request.getParams());
                                    Response response = new Response();
                                    response.setResult(result);
                                    ctx.channel().writeAndFlush(response);
                                }
                            });
                        }
                    });

            bootstrap.bind(port).sync();
        }catch (Throwable e) {
            throw new RuntimeException("服务器启动异常");
        }

    }

    public <I> void register(Class<I> serverInterface,I serverInstance) throws IllegalAccessException {
        register.register(serverInterface,serverInstance);
    }

    public void stop(){
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }


}
