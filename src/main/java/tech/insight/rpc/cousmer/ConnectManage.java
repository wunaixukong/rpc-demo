package tech.insight.rpc.cousmer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectManage {

    private final Map<String,ChannelWrapper> connectMap = new ConcurrentHashMap<>();

    private final Bootstrap bootstrap;

    public ConnectManage(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Channel findChannel(String host, int port) {
        String key = host + ":" + port;
        ChannelWrapper channelWrapper = connectMap.computeIfAbsent(key, (k) -> {
            try {
                ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
                Channel channel = channelFuture.channel();
                channel.closeFuture().addListener(f -> {
                    connectMap.remove(key);
                });
                return new ChannelWrapper(channel);
            } catch (InterruptedException e) {
                return new ChannelWrapper(null);
            }
        });


        Channel channel = channelWrapper.channel;
        if (channel == null || !channel.isActive()) {
            connectMap.remove(key);
        }
        return channel;
    }

    private static class ChannelWrapper{
        private final Channel channel;


        private ChannelWrapper(Channel channel) {
            this.channel = channel;
        }
    }

}
