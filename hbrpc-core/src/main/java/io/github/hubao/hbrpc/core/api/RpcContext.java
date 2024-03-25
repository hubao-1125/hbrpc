package io.github.hubao.hbrpc.core.api;

import io.github.hubao.hbrpc.core.meta.InstanceMeta;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString
public class RpcContext {

    List<Filter> filters;

    Router<InstanceMeta> router;

    LoadBalancer<InstanceMeta> loadBalancer;

    private Map<String, String> parameters;
}
