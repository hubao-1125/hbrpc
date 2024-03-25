package io.github.hubao.hbrpc.core.consumer;

import io.github.hubao.hbrpc.core.api.Filter;
import io.github.hubao.hbrpc.core.api.RpcContext;
import io.github.hubao.hbrpc.core.api.RpcRequest;
import io.github.hubao.hbrpc.core.api.RpcResponse;
import io.github.hubao.hbrpc.core.meta.InstanceMeta;
import io.github.hubao.hbrpc.core.util.TypeUtils;
import io.github.hubao.hbrpc.core.util.http.HttpInvoker;
import io.github.hubao.hbrpc.core.util.MethodUtils;
import io.github.hubao.hbrpc.core.util.http.OkHttpInvoker;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

@Slf4j
public class HbInvocationHandler implements InvocationHandler {

    Class<?> service;
    RpcContext context;
    List<InstanceMeta> providers;

    HttpInvoker httpInvoker = new OkHttpInvoker();

    public HbInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
        this.service = clazz;
        this.context = context;
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

        for (Filter filter : this.context.getFilters()) {
            Object preResult = filter.prefilter(rpcRequest);
            if(preResult != null) {
                log.debug(filter.getClass().getName() + " ==> prefilter: " + preResult);
                return preResult;
            }
        }

        List<InstanceMeta> instances = context.getRouter().route(providers);
        InstanceMeta instance = context.getLoadBalancer().choose(instances);
        log.debug("loadBalancer.choose(instances) ==> " + instance);

        RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());
        Object result = castReturnResult(method, rpcResponse);

        for (Filter filter : this.context.getFilters()) {
            Object filterResult = filter.postfilter(rpcRequest, rpcResponse, result);
            if(filterResult != null) {
                return filterResult;
            }
        }

        return result;
    }

    private static Object castReturnResult(Method method, RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()) {
            Object data = rpcResponse.getData();
            return TypeUtils.castMethodResult(method, data);
        } else {
            Exception ex = rpcResponse.getEx();
            throw new RuntimeException(ex);
        }
    }
}
