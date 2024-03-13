package io.github.hubao.hbrpc.demo.consumer;

import io.github.hubao.hbrpccore.annotation.HbConsumer;
import io.github.hubao.hbrpccore.consumer.ConsumerConfig;
import io.github.hubao.hbrpcdemoapi.User;
import io.github.hubao.hbrpcdemoapi.UserService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ConsumerConfig.class})
public class HbrpcDemoConsumerApplication {

    @HbConsumer
    UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(HbrpcDemoConsumerApplication.class, args);
    }


    @Bean
    public ApplicationRunner consumer_runner() {
        return x -> {
//            User user = userService.findById(1);
//            System.out.println("RPC result userService.findById(1) = " + user);

//            System.out.println(userService.toString());
//
//            System.out.println(userService.getId(11));

            System.out.println(userService.getName());

            //Order order = orderService.findById(2);
            //System.out.println("RPC result orderService.findById(2) = " + order);

            //demo2.test();

//            Order order404 = orderService.findById(404);
//            System.out.println("RPC result orderService.findById(2) = " + order404);

        };
    }
}
