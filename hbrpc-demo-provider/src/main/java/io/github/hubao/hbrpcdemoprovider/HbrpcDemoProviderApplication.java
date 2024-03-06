package io.github.hubao.hbrpcdemoprovider;

import io.github.hubao.hbrpccore.api.RpcRequest;
import io.github.hubao.hbrpccore.api.RpcResponse;
import io.github.hubao.hbrpccore.provider.ProviderBootstrap;
import io.github.hubao.hbrpccore.provider.ProviderConfig;
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

    @Autowired
    private ProviderBootstrap providerBootstrap;


    public static void main(String[] args) {
        SpringApplication.run(HbrpcDemoProviderApplication.class, args);
    }


    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {

        return providerBootstrap.invoke(request);
    }


    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            RpcRequest request = new RpcRequest();
            request.setService("io.github.hubao.hbrpcdemoapi.UserService");
            request.setMethod("findById");
            request.setArgs(new Object[]{100});
            RpcResponse rpcResponse = invoke(request);
            System.out.println("return:" + rpcResponse.getData());
        };
    }
}
