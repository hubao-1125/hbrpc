package io.github.hubao.hbrpc.core.filter;

import io.github.hubao.hbrpc.core.api.Filter;
import io.github.hubao.hbrpc.core.api.RpcRequest;
import io.github.hubao.hbrpc.core.api.RpcResponse;
import io.github.hubao.hbrpc.core.util.MethodUtils;
import io.github.hubao.hbrpc.core.util.MockUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MockFilter implements Filter {
    @SneakyThrows
    @Override
    public Object prefilter(RpcRequest request) {
        Class service = Class.forName(request.getService());
        Method method = findMethod(service, request.getMethodSign());
        Class clazz = method.getReturnType();
        return MockUtils.mock(clazz);
    }

    private Method findMethod(Class service, String methodSign) {
        return Arrays.stream(service.getMethods())
                .filter(method -> !MethodUtils.checkLocalMethod(method))
                .filter(method -> methodSign.equals(MethodUtils.methodSign(method)))
                .findFirst().orElse(null);
    }

    @Override
    public Object postfilter(RpcRequest request, RpcResponse response, Object result) {
        return null;
    }
}
