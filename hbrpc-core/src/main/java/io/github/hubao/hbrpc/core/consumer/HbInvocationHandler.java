package io.github.hubao.hbrpc.core.consumer;

import io.github.hubao.hbrpc.core.api.*;
import io.github.hubao.hbrpc.core.governance.SlidingTimeWindow;
import io.github.hubao.hbrpc.core.meta.InstanceMeta;
import io.github.hubao.hbrpc.core.util.MethodUtils;
import io.github.hubao.hbrpc.core.util.TypeUtils;
import io.github.hubao.hbrpc.core.consumer.http.OkHttpInvoker;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HbInvocationHandler implements InvocationHandler {

    Class<?> service;
    RpcContext context;
    final List<InstanceMeta> providers;

    final List<InstanceMeta> isolateProviders = new ArrayList<>();

    final List<InstanceMeta> halfOpenProviders = new ArrayList<>();

    HttpInvoker httpInvoker;

    Map<String, SlidingTimeWindow> windows = new HashMap<>();

    ScheduledExecutorService executor;

    public HbInvocationHandler(Class<?> clazz, RpcContext context, List<InstanceMeta> providers) {
        this.service = clazz;
        this.context = context;
        this.providers = providers;
        int timeout = Integer.parseInt(context.getParameters()
                .getOrDefault("app.timeout", "1000"));
        this.httpInvoker = new OkHttpInvoker(timeout);
        this.executor = Executors.newScheduledThreadPool(1);
        this.executor.scheduleWithFixedDelay(this::halfOpen, 10, 60, TimeUnit.SECONDS);
    }

    private void halfOpen() {

        log.info("half open isolateProviders ===> {}", isolateProviders);
        halfOpenProviders.clear();
        halfOpenProviders.addAll(isolateProviders);
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

        int retries = Integer.parseInt(context.getParameters()
                .getOrDefault("app.retries", "1"));

        while (retries -- > 0) {
            log.debug(" ===> reties: " + retries);
            try {
                for (Filter filter : this.context.getFilters()) {
                    Object preResult = filter.prefilter(rpcRequest);
                    if (preResult != null) {
                        log.debug(filter.getClass().getName() + " ==> prefilter: " + preResult);
                        return preResult;
                    }
                }

                InstanceMeta instance;
                synchronized (halfOpenProviders) {
                    if (halfOpenProviders.isEmpty()) {
                        List<InstanceMeta> instances = context.getRouter().route(providers);
                        instance = context.getLoadBalancer().choose(instances);
                        log.debug("loadBalancer.choose(instances) ==> " + instance);
                    } else {
                        instance = halfOpenProviders.remove(0);
                        log.info(" check alive instance ==> {}", instance);
                    }
                }

                RpcResponse<?> rpcResponse = null;
                Object result = null;
                String url = instance.toUrl();
                try {
                    rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());
                    result = castReturnResult(method, rpcResponse);
                } catch (Exception e) {

                    synchronized (instance) {
                        SlidingTimeWindow window = windows.get(url);
                        if (window == null) {
                            window = new SlidingTimeWindow();
                            windows.put(url, window);
                        }
                        window.record(System.currentTimeMillis());
                        log.info("instance {} in window with {}", url, window.getSum());

                        if (window.getSum() >= 5) {
                            isolate(instance);
                        }
                    }
                    throw e;
                }

                synchronized (providers) {
                    if (!providers.contains(instance)) {
                        isolateProviders.remove(instance);
                        providers.add(instance);
                        log.info("instance {} is recovered, isolateProviders={}, providers={}", instance, isolateProviders, providers);
                    }
                }

                for (Filter filter : this.context.getFilters()) {
                    Object filterResult = filter.postfilter(rpcRequest, rpcResponse, result);
                    if (filterResult != null) {
                        return filterResult;
                    }
                }
                return result;
            } catch (Exception ex) {
                if (!(ex.getCause() instanceof SocketTimeoutException)) {
                    throw ex;
                }
            }
        }
        return null;
    }

    private void isolate(InstanceMeta instance) {

        log.info(" ===> isolate instance {}", instance);
        providers.remove(instance);
        log.info(" ===> providers = {}", providers);

        isolateProviders.add(instance);
        log.info(" ===> isolateProviders = {}", isolateProviders);
    }

    private static Object castReturnResult(Method method, RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()) {
            return TypeUtils.castMethodResult(method, rpcResponse.getData());
        } else {
            Exception exception = rpcResponse.getEx();
            if(exception instanceof RpcException ex) {
                throw ex;
            } else {
                throw new RpcException(exception, RpcException.UNKNOWN_EX);
            }
        }
    }
}
