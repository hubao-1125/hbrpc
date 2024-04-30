package io.github.hubao.hbrpc.core.filter;

import io.github.hubao.hbrpc.core.api.Filter;
import io.github.hubao.hbrpc.core.api.RpcContext;
import io.github.hubao.hbrpc.core.api.RpcRequest;
import io.github.hubao.hbrpc.core.api.RpcResponse;

import java.util.Map;

public class ContextParameterFilter implements Filter {
    @Override
    public Object prefilter(RpcRequest request) {
        Map<String, String> params = RpcContext.ContextParameters.get();
        if(!params.isEmpty()) {
            request.getParams().putAll(params);
        }
        return null;
    }

    @Override
    public Object postfilter(RpcRequest request, RpcResponse response, Object result) {
        RpcContext.ContextParameters.get().clear();
        return null;
    }
}
