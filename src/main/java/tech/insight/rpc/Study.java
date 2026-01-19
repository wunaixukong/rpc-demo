package tech.insight.rpc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.time.Year;
import java.util.Date;

public class Study  {
    

    public static void main(String[] args) {
        Year year = Year.of(2020);

        // 1. 序列化：强制指定格式，生成 "2020" 而不是 {"value":2020...}
        String yearStr = JSON.toJSONString(year, "yyyy");
        System.out.println("序列化结果: " + yearStr); // 输出 "2020"

        // 2. 反序列化
        Year jsonYear = JSON.parseObject(yearStr, Year.class);
        System.out.println("结果类型: " + jsonYear.getClass());
        System.out.println("结果值: " + jsonYear);
    }
}
