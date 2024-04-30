package io.github.hubao.hbrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;


@Data
@ConfigurationProperties(prefix = "hbrpc.provider")
public class ProviderProperties {

    Map<String, String> metas = new HashMap<>();

    String test;


}
