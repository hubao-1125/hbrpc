package io.github.hubao.hbrpc.core.provider;

import io.github.hubao.hbrpc.core.annotation.HbProvider;
import io.github.hubao.hbrpc.core.api.RegistryCenter;
import io.github.hubao.hbrpc.core.meta.InstanceMeta;
import io.github.hubao.hbrpc.core.meta.ProviderMeta;
import io.github.hubao.hbrpc.core.meta.ServiceMeta;
import io.github.hubao.hbrpc.core.util.MethodUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.*;

@Data
@Slf4j
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;
    RegistryCenter rc;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();
    private InstanceMeta instance;

    @Value("${server.port}")
    private String port;

    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String env;

    @SneakyThrows
    @PostConstruct  // init-method
    public void init() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(HbProvider.class);
        rc = applicationContext.getBean(RegistryCenter.class);
        providers.keySet().forEach(System.out::println);
        providers.values().forEach(this::genInterface);
    }

    @SneakyThrows
    public void start() {
        String ip = InetAddress.getLocalHost().getHostAddress();
        instance = InstanceMeta.http(ip, Integer.valueOf(port));
        rc.start();
        skeleton.keySet().forEach(this::registerService);
    }

    @PreDestroy
    public void stop() {
        skeleton.keySet().forEach(this::unregisterService);
        rc.stop();
    }

    private void registerService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(app).namespace(namespace).env(env).name(service).build();
        rc.register(serviceMeta, instance);
    }

    private void unregisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(app).namespace(namespace).env(env).name(service).build();
        rc.unregister(serviceMeta, instance);
    }

    private void genInterface(Object impl) {
        Arrays.stream(impl.getClass().getInterfaces()).forEach(
                service -> {
                    Arrays.stream(service.getMethods())
                            .filter(method -> !MethodUtils.checkLocalMethod(method))
                            .forEach( method -> { createProvider(service, impl, method);});
                });
    }

    private void createProvider(Class<?> service, Object impl, Method method) {
        ProviderMeta providerMeta = ProviderMeta.builder().method(method)
                .serviceImpl(impl).methodSign(MethodUtils.methodSign(method)).build();
        log.info(" create a provider: " + providerMeta);
        skeleton.add(service.getCanonicalName(), providerMeta);
    }
}
