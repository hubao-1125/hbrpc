package io.github.hubao.hbrpc.core.filter;

import io.github.hubao.hbrpc.core.api.Filter;
import io.github.hubao.hbrpc.core.api.RpcRequest;
import io.github.hubao.hbrpc.core.api.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CacheFilter implements Filter {

    // 替换成guava cache，加容量和过期时间 todo 71
    static Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object prefilter(RpcRequest request) {
        return cache.get(request.toString());
    }

    @Override
    public Object postfilter(RpcRequest request, RpcResponse response, Object result)  {
        cache.putIfAbsent(request.toString(), result);
        return result;
    }
}
