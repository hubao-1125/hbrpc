package io.github.hubao.hbrpc.demo.provider;

import io.github.hubao.hbrpc.core.api.RpcRequest;
import io.github.hubao.hbrpc.core.api.RpcResponse;
import io.github.hubao.hbrpc.core.provider.ProviderBootstrap;
import io.github.hubao.hbrpc.core.provider.ProviderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import(ProviderConfig.class)
public class HbrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(HbrpcDemoProviderApplication.class, args);
    }

    // 使用HTTP + JSON 来实现序列化和通信

    @Autowired
    ProviderBootstrap providerBootstrap;

    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        return providerBootstrap.invoke(request);
    }


    @Bean
    ApplicationRunner providerRun() {
        return x -> {
//            // test 1 parameter method
//            RpcRequest request = new RpcRequest();
//            request.setService("cn.kimmking.kkrpc.demo.api.UserService");
//            request.setMethodSign("findById@1_int");
//            request.setArgs(new Object[]{100});
//
//            RpcResponse rpcResponse = invoke(request);
//            System.out.println("return : "+rpcResponse.getData());
//
//            // test 2 parameters method
//            RpcRequest request1 = new RpcRequest();
//            request1.setService("cn.kimmking.kkrpc.demo.api.UserService");
//            request1.setMethodSign("findById@2_int_java.lang.String");
//            request1.setArgs(new Object[]{100, "CC"});
//
//            RpcResponse rpcResponse1 = invoke(request1);
//            System.out.println("return : "+rpcResponse1.getData());

        };
    }


}
