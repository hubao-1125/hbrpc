package io.github.hubao.hbrpc.core.api;

import io.github.hubao.hbrpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.List;

@Data
public class RpcContext {

    List<Filter> filters;

    Router<InstanceMeta> router;

    LoadBalancer<InstanceMeta> loadBalancer;
}
