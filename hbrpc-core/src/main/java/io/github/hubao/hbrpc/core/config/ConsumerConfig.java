package io.github.hubao.hbrpc.core.config;

import io.github.hubao.hbrpc.core.api.*;
import io.github.hubao.hbrpc.core.cluster.GrayRouter;
import io.github.hubao.hbrpc.core.cluster.RoundRibonLoadBalancer;
import io.github.hubao.hbrpc.core.consumer.ConsumerBootstrap;
import io.github.hubao.hbrpc.core.filter.ContextParameterFilter;
import io.github.hubao.hbrpc.core.meta.InstanceMeta;
import io.github.hubao.hbrpc.core.registry.hb.HbRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;

@Slf4j
@Configuration
@Import({AppProperties.class, ConsumerProperties.class})
public class ConsumerConfig {

    @Autowired
    AppProperties appProperties;

    @Autowired
    ConsumerProperties consumerProperties;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    ApolloChangedListener consumer_apolloChangedListener() {
        return new ApolloChangedListener();
    }

    @Bean
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE + 1)
    public ApplicationRunner consumerBootstrap_runner(@Autowired ConsumerBootstrap consumer) {
        return x -> {
            log.info("consumerBootstrap starting ...");
            consumer.start();
            log.info("consumerBootstrap started ...");
        };
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        return new RoundRibonLoadBalancer<>();
    }

    @Bean
    public Router<InstanceMeta> router() {
        return new GrayRouter(consumerProperties.getGrayRatio());
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter consumer_rc() {
        return new HbRegistryCenter();
    }

    @Bean
    public Filter defaultFilter() {
        return new ContextParameterFilter();
    }

    @Bean
    @RefreshScope // context.refresh
    public RpcContext createContext(@Autowired Router router,
                                    @Autowired LoadBalancer loadBalancer,
                                    @Autowired List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.getParameters().put("app.id", appProperties.getId());
        context.getParameters().put("app.namespace", appProperties.getNamespace());
        context.getParameters().put("app.env", appProperties.getEnv());
        context.setConsumerProperties(consumerProperties);
        return context;
    }

}
