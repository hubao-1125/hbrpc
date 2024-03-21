package io.github.hubao.hbrpc.core.provider;

import io.github.hubao.hbrpc.core.api.RpcRequest;
import io.github.hubao.hbrpc.core.api.RpcResponse;
import io.github.hubao.hbrpc.core.meta.ProviderMeta;
import io.github.hubao.hbrpc.core.meta.ServiceMeta;
import io.github.hubao.hbrpc.core.util.TypeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class ProviderInvoker {

    private MultiValueMap<ServiceMeta, ProviderMeta> skeleton;

    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String env;

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
    }

    public RpcResponse invoke(RpcRequest request) {
        RpcResponse rpcResponse = new RpcResponse();
        List<ProviderMeta> providerMetas = skeleton.get(ServiceMeta.builder().app(app).namespace(namespace).env(env)
                .name(request.getService()).version(request.getVersion() == null ? "" : request.getVersion())
                .build());
        try {
            ProviderMeta meta = findProviderMeta(providerMetas, request.getMethodSign());
            Method method = meta.getMethod();
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes());
            Object result = method.invoke(meta.getServiceImpl(), args);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (InvocationTargetException e) {
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e) {
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        }
        return rpcResponse;
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes) {
        if(args == null || args.length == 0) return args;
        Object[] actuals = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            actuals[i] = TypeUtils.cast(args[i], parameterTypes[i]);
        }
        return actuals;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElse(null);
    }
}
