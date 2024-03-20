package io.github.hubao.hbrpc.core.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.github.hubao.hbrpc.core.api.RpcContext;
import io.github.hubao.hbrpc.core.api.RpcRequest;
import io.github.hubao.hbrpc.core.api.RpcResponse;
import io.github.hubao.hbrpc.core.util.MethodUtils;
import io.github.hubao.hbrpc.core.util.TypeUtils;
import io.github.hubao.hbrpccore.api.*;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HbInvocationHandler implements InvocationHandler {

    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");

    Class<?> service;

    RpcContext rpcContext;
    List<String> providers;

    public HbInvocationHandler(Class<?> clazz, RpcContext rpcContext, List<String> providers) {
        this.service = clazz;
        this.rpcContext = rpcContext;
        this.providers = providers;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (MethodUtils.checkLocalMethod(method.getName())) {
            return null;
        }

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.methodSign(method));
        rpcRequest.setArgs(args);

        List<String> route = rpcContext.getRouter().route(this.providers);
        String url = (String) rpcContext.getLoadBalancer().choose(route);
        System.out.println("loadBalancer.choose(route) ==>" + url);

        RpcResponse rpcResponse = post(rpcRequest, url);

        if(rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            if(data instanceof JSONObject jsonResult) {
                return jsonResult.toJavaObject(method.getReturnType());
            } else if (data instanceof JSONArray jsonArray) {
                Object[] array = jsonArray.toArray();
                Class<?> componentType = method.getReturnType().getComponentType();
                Object resultArray = Array.newInstance(componentType, array.length);
                for (int i = 0; i < array.length; i++) {
                    Array.set(resultArray, i, array[i]);
                }
                return resultArray;
            } else {
                return TypeUtils.cast(data, method.getReturnType());
            }
        }else {
            Exception ex = rpcResponse.getEx();
            //ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();

    private RpcResponse post(RpcRequest rpcRequest, String url) {
        String reqJson = JSON.toJSONString(rpcRequest);
        System.out.println(" ===> reqJson = " + reqJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSONTYPE))
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            System.out.println(" ===> respJson = " + respJson);
            RpcResponse rpcResponse = JSON.parseObject(respJson, RpcResponse.class);
            return rpcResponse;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
