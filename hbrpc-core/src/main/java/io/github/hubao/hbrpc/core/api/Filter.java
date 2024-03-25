package io.github.hubao.hbrpc.core.api;

public interface Filter {

    Object prefilter(RpcRequest request);

    Object postfilter(RpcRequest request, RpcResponse response, Object result);

    // Filter next();

    // A -> B -> C 有问题的
    // - -> - -> D 还有问题
    // - -> D  Mock

    Filter Default = new Filter() {
        @Override
        public RpcResponse prefilter(RpcRequest request) {
            return null;
        }

        @Override
        public Object postfilter(RpcRequest request, RpcResponse response, Object result) {
            return null;
        }
    };

}
