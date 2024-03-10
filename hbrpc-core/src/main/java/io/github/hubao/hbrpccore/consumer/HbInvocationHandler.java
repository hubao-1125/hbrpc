package io.github.hubao.hbrpccore.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.hubao.hbrpccore.api.RpcRequest;
import io.github.hubao.hbrpccore.api.RpcResponse;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class HbInvocationHandler implements InvocationHandler {

    final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    Class<?> service;

    public HbInvocationHandler(Class<?> clazz) {
        this.service = clazz;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethod(method.getName());
        rpcRequest.setArgs(args);

        RpcResponse rpcResponse = post(rpcRequest);

        if (rpcResponse.isStatus()) {
            JSONObject jsonObject = (JSONObject) rpcResponse.getData();
            return jsonObject.toJavaObject(method.getReturnType());
        } else {
            Exception ex = rpcResponse.getEx();
            throw ex;
        }

    }

    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();

    private RpcResponse post(RpcRequest rpcRequest) {

        String req = JSON.toJSONString(rpcRequest);
        Request request = new Request.Builder()
                .url("http://localhost:8080/")
                .post(RequestBody.create(req, JSON_TYPE))
                .build();

        try {
            String resJson = client.newCall(request).execute().body().string();
            RpcResponse rpcResponse = JSON.parseObject(resJson, RpcResponse.class);
            return rpcResponse;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
