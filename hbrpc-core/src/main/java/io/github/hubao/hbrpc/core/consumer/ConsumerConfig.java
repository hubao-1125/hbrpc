package io.github.hubao.hbrpc.core.consumer;

import io.github.hubao.hbrpc.core.api.LoadBalancer;
import io.github.hubao.hbrpc.core.api.RegistryCenter;
import io.github.hubao.hbrpc.core.api.Router;
import io.github.hubao.hbrpc.core.cluster.RoundRibonLoadBalancer;
import io.github.hubao.hbrpc.core.registry.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfig {

    @Value("${hbrpc.providers}")
    String servers;

    @Bean
    ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerBootstrap_runner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            System.out.println("consumerBootstrap starting ...");
            consumerBootstrap.start();
            System.out.println("consumerBootstrap started ...");
        };
    }


    @Bean
    public LoadBalancer loadBalancer() {
        return new RoundRibonLoadBalancer();
    }

    @Bean
    public Router router() {
        return Router.Default;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumer_rc() {
        return new ZkRegistryCenter();
    }

}
