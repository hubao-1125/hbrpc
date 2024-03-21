package io.github.hubao.hbrpc.core.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

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
    private String version;



    public String toPath() {
        if (StringUtils.isBlank(version)) {
            return String.format("%s_%s_%s_%s", app, namespace, env, name);
        }
        return String.format("%s_%s_%s_%s?version=%s", app, namespace, env, name, version);
    }

}
