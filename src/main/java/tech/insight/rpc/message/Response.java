package tech.insight.rpc.message;

import lombok.Data;

@Data
public class Response {
    private Object result;

    private int code;

    private String errorMsg;


    public static Response success(Object result){
        Response response = new Response();
        response.setResult(result);
        response.setCode(200);
        return response;
    }

    public static Response fail(String msg){
        Response response = new Response();
        response.setErrorMsg(msg);
        response.setCode(400);
        return response;
    }
}
