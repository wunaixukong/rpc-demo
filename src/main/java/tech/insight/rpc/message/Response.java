package tech.insight.rpc.message;

import lombok.Data;

@Data
public class Response {
    private Integer requestId;

    private Object result;

    private int code;

    private String errorMsg;


    public static Response success(Object result,Integer requestId) {
        Response response = new Response();
        response.setResult(result);
        response.setCode(200);
        response.setRequestId(requestId);
        return response;
    }

    public static Response fail(String msg,Integer requestId){
        Response response = new Response();
        response.setErrorMsg(msg);
        response.setCode(400);
        response.setRequestId(requestId);
        return response;
    }
}
