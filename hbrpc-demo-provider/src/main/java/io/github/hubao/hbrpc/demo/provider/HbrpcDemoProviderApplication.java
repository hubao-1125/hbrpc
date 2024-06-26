package io.github.hubao.hbrpc.demo.provider;

import io.github.hubao.hbrpc.core.api.RpcRequest;
import io.github.hubao.hbrpc.core.api.RpcResponse;
import io.github.hubao.hbrpc.core.provider.ProviderInvoker;
import io.github.hubao.hbrpc.demo.api.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Import(ProviderConfig.class)
public class HbrpcDemoProviderApplication {

    @Autowired
    Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(HbrpcDemoProviderApplication.class, args);
    }

    // 使用HTTP + JSON 来实现序列化和通信

    @Autowired
    ProviderInvoker providerInvoker;

    @RequestMapping("/")
    public RpcResponse invoke(@RequestBody RpcRequest request) {
        return providerInvoker.invoke(request);
    }

    @Autowired
    private UserService userService;

    @RequestMapping("/ports")
    public RpcResponse ports(@RequestParam("ports") String ports) {
        userService.setTimeoutPort(ports);
        RpcResponse<String> response = new RpcResponse<>();
        response.setStatus(true);
        response.setData("OK:" + ports);
        return response;
    }


    @Bean
    ApplicationRunner providerRun() {
        return x -> {
            // test 1 parameter method
            RpcRequest request = new RpcRequest();
            request.setService("io.github.hubao.hbrpc.demo.api.UserService");
            request.setMethodSign("findById@1_int");
            request.setArgs(new Object[]{100});

            RpcResponse rpcResponse = invoke(request);
            System.out.println("return : "+rpcResponse.getData());

            // test 2 parameters method
            RpcRequest request1 = new RpcRequest();
            request1.setService("io.github.hubao.hbrpc.demo.api.UserService");
            request1.setMethodSign("findById@2_int_java.lang.String");
            request1.setArgs(new Object[]{100, "CC"});

            RpcResponse rpcResponse1 = invoke(request1);
            System.out.println("return : "+rpcResponse1.getData());

            System.out.println("========================================");
            System.out.println("server.port ===> "+environment.getProperty("server.port"));
        };
    }


}
