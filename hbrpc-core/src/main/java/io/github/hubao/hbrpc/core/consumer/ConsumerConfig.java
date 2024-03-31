package io.github.hubao.hbrpc.core.consumer;

import io.github.hubao.hbrpc.core.api.Filter;
import io.github.hubao.hbrpc.core.api.LoadBalancer;
import io.github.hubao.hbrpc.core.api.RegistryCenter;
import io.github.hubao.hbrpc.core.api.Router;
import io.github.hubao.hbrpc.core.cluster.GrayRouter;
import io.github.hubao.hbrpc.core.cluster.RoundRibonLoadBalancer;
import io.github.hubao.hbrpc.core.filter.CacheFilter;
import io.github.hubao.hbrpc.core.meta.InstanceMeta;
import io.github.hubao.hbrpc.core.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Slf4j
public class ConsumerConfig {

    @Value("${hbrpc.providers}")
    String servers;

    @Bean
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE + 1)
    public ApplicationRunner consumerBootstrap_runner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            log.info("consumerBootstrap starting ...");
            consumerBootstrap.start();
            log.info("consumerBootstrap started ...");
        };
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        //return LoadBalancer.Default;
        return new RoundRibonLoadBalancer<>();
    }

    @Value("${app.grayRatio}")
    private int grayRatio;

    @Bean
    public Router<InstanceMeta> router() {
        return new GrayRouter(grayRatio);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter consumer_rc() {
        return new ZkRegistryCenter();
    }

    @Bean
    public Filter defaultFilter() {
        return Filter.Default;
    }

//    @Bean
//    public Filter filter1() {
//        return new CacheFilter();
//    }

//    @Bean
//    public Filter filter2() {
//        return new MockFilter();
//    }



}
