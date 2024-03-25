package io.github.hubao.hbrpc.core.util.http;

import io.github.hubao.hbrpc.core.api.RpcRequest;
import io.github.hubao.hbrpc.core.api.RpcResponse;

public interface HttpInvoker {

    RpcResponse<?> post(RpcRequest rpcRequest, String url);
}
