package io.github.hubao.hbrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kkrpc.app")
public class AppProperties {

    // for app instance
    private String id = "app1";

    private String namespace = "public";

    private String env = "dev";

}
