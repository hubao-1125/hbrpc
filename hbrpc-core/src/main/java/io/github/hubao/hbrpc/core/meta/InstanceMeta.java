package io.github.hubao.hbrpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述: 实例元数据
 * @author hubao
 * @Date: 2024/3/20 21:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"scheme", "host", "port", "context"})
public class InstanceMeta {

    private String scheme;
    private String host;
    private Integer port;
    private String context;

    private boolean status;
    private Map<String, String> parameters = new HashMap<>();

    public InstanceMeta(String scheme, String host, Integer port, String context) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", scheme, host, port, context);
    }

    public static InstanceMeta http(String host, Integer port) {
        return new InstanceMeta("http", host, port, "hbrpc");
    }

    public InstanceMeta addParams(Map<String, String> params) {
        this.getParameters().putAll(params);
        return this;
    }

    public String toMetas() {
        return JSON.toJSONString(this.getParameters());
    }
}
