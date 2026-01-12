package tech.insight.rpc.codec;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import tech.insight.rpc.message.Message;
import tech.insight.rpc.message.Request;
import tech.insight.rpc.message.Response;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class AlinDecoder extends LengthFieldBasedFrameDecoder {

    public AlinDecoder() {
        super(1024 * 1024, 0,
                4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        byte[] magic = new byte[Message.MAGIC.length];
        frame.readBytes(magic);
        if (!Arrays.equals(magic, Message.MAGIC)) {
            throw new IllegalAccessException("魔数不对!");
        }
        byte type = frame.readByte();
        byte[] body = new byte[frame.readableBytes()];
        frame.readBytes(body);
        if (Objects.equals(type,Message.Type.REQUEST.code)) {
            return deserialzeRequest(body);
        }
        if (Objects.equals(type,Message.Type.RESPONSE.code)) {
            return deserialzeResponse(body);
        }
        throw new IllegalAccessException("消息类型不支持," + type);
    }

    private Response deserialzeResponse(byte[] body) {
        return JSONObject.parseObject(new String(body, StandardCharsets.UTF_8), Response.class);
    }

    private Request deserialzeRequest(byte[] body) {
        return JSONObject.parseObject(new String(body, StandardCharsets.UTF_8), Request.class, JSONReader.Feature.SupportClassForName);
    }

}
