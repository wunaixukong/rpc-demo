package tech.insight.rpc.message;

import lombok.Data;

import java.nio.charset.StandardCharsets;

/**
 * 1.长度
 * 2.魔数
 * 3.类型
 * 4.内容
 */
@Data
public class Message {
    public static final byte[] MAGIC = "阿林".getBytes(StandardCharsets.UTF_8);

    private byte[] magic;

    private int length;

    private byte type;

    private String body;

    public enum Type {
        REQUEST(1),
        RESPONSE(2);

        public final byte code;

        Type(int code) {
            this.code = (byte) code;
        }
    }

}
