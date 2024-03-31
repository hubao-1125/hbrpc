package io.github.hubao.hbrpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述: 服务元数据
 * @author hubao
 * @Date: 2024/3/20 21:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceMeta {

    private String app;
    private String namespace;
    private String env;
    private String name;

    private Map<String, String> parameters = new HashMap<>();



    public String toPath() {
        return String.format("%s_%s_%s_%s", app, namespace, env, name);
    }

    public String toMetas() {
        return JSON.toJSONString(this.getParameters());
    }

}
