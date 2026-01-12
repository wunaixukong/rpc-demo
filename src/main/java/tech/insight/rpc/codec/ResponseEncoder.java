package tech.insight.rpc.codec;

import com.alibaba.fastjson2.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import tech.insight.rpc.message.Message;
import tech.insight.rpc.message.Response;

import java.nio.charset.StandardCharsets;

public class ResponseEncoder extends MessageToByteEncoder<Response> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Response response, ByteBuf out) throws Exception {
        byte[] magic = Message.MAGIC;
        byte code = Message.Type.REQUEST.code;
        byte[] body = serializeResponse(response);
        int length = magic.length + Byte.BYTES + body.length;

        out.writeInt(length);
        out.writeBytes(magic);
        out.writeByte(code);
        out.writeBytes(body);

    }

    private byte[] serializeResponse(Response request) {
        return JSONObject.toJSONString(request).getBytes(StandardCharsets.UTF_8);
    }
}
