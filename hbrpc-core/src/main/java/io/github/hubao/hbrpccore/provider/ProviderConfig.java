package io.github.hubao.hbrpccore.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// 让 spring 管理
public class ProviderConfig {

    @Bean
    // 启动项目直接加载 bootStrap
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
    }
}
