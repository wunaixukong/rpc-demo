package tech.insight.rpc.codec;

import com.alibaba.fastjson2.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import tech.insight.rpc.message.Message;
import tech.insight.rpc.message.Request;

import java.nio.charset.StandardCharsets;

public class RequestEncoder extends MessageToByteEncoder<Request> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Request request, ByteBuf out) throws Exception {
        byte[] magic = Message.MAGIC;
        byte code = Message.Type.REQUEST.code;
        byte[] body = serializeRequest(request);
        int length = magic.length + Byte.BYTES + body.length;

        out.writeInt(length);
        out.writeBytes(magic);
        out.writeByte(code);
        out.writeBytes(body);

    }

    private byte[] serializeRequest(Request request) {
        return JSONObject.toJSONString(request).getBytes(StandardCharsets.UTF_8);
    }
}
