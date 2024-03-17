package io.github.hubao.hbrpccore.consumer;

import io.github.hubao.hbrpccore.api.LoadBalancer;
import io.github.hubao.hbrpccore.api.RegistryCenter;
import io.github.hubao.hbrpccore.api.Router;
import io.github.hubao.hbrpccore.cluster.RandomLoadBalancer;
import io.github.hubao.hbrpccore.cluster.RoundRibonLoadBalancer;
import io.github.hubao.hbrpccore.registry.ZkRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

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
