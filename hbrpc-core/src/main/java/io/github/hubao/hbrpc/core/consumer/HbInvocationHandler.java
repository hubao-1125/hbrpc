package io.github.hubao.hbrpc.core.consumer;

import io.github.hubao.hbrpc.core.api.RpcContext;
import io.github.hubao.hbrpc.core.api.RpcRequest;
import io.github.hubao.hbrpc.core.api.RpcResponse;
import io.github.hubao.hbrpc.core.util.http.HttpInvoker;
import io.github.hubao.hbrpc.core.util.MethodUtils;
import io.github.hubao.hbrpc.core.util.http.OkHttpInvoker;
import io.github.hubao.hbrpc.core.util.TypeUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class HbInvocationHandler implements InvocationHandler {

    Class<?> service;

    RpcContext rpcContext;
    List<String> providers;

    HttpInvoker httpInvoker = new OkHttpInvoker();

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

        RpcResponse rpcResponse = httpInvoker.postRpcRequest(rpcRequest, url);


        if(rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return TypeUtils.castByMethod(data, method);
        }else {
            Exception ex = rpcResponse.getEx();
            throw new RuntimeException(ex);
        }
    }
}
