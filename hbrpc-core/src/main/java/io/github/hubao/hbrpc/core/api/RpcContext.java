package io.github.hubao.hbrpc.core.api;

import io.github.hubao.hbrpc.core.config.ConsumerProperties;
import io.github.hubao.hbrpc.core.meta.InstanceMeta;
import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ToString
public class RpcContext {

    List<Filter> filters;

    Router<InstanceMeta> router;

    LoadBalancer<InstanceMeta> loadBalancer;

    private Map<String, String> parameters = new HashMap<>();

    private ConsumerProperties consumerProperties;

    public String param(String key) {
        return parameters.get(key);
    }

    public static ThreadLocal<Map<String,String>> ContextParameters = new ThreadLocal<>() {
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<>();
        }
    };

    public static void setContextParameter(String key, String value) {
        ContextParameters.get().put(key, value);
    }

    public static String getContextParameter(String key) {
        return ContextParameters.get().get(key);
    }

    public static void removeContextParameter(String key) {
        ContextParameters.get().remove(key);
    }
}
