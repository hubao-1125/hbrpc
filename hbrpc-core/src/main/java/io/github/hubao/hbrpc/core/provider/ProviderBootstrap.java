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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.*;

@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private MultiValueMap<ServiceMeta, ProviderMeta> skeleton = new LinkedMultiValueMap<>();
    private InstanceMeta instance;

    @Value("${server.port}")
    private String port;

    @Value("${app.id}")
    private String app;

    @Value("${app.namespace}")
    private String namespace;

    @Value("${app.env}")
    private String env;

    RegistryCenter rc;

    @PostConstruct  // init-method
    public void init() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(HbProvider.class);
        rc = applicationContext.getBean(RegistryCenter.class);
        providers.forEach((x,y) -> System.out.println(x));
        providers.values().forEach(this::genInterface);
    }


    @SneakyThrows
    public void start() {
        String ip = InetAddress.getLocalHost().getHostAddress();
        instance = InstanceMeta.http(ip, Integer.parseInt(port));
        rc.start();
        skeleton.keySet().forEach(this::registryService);
    }

    @PreDestroy
    public void stop() {
        skeleton.keySet().forEach(this::unRegistryService);
        rc.stop();
    }

    private void unRegistryService(ServiceMeta serviceMeta) {
        rc.unregister(serviceMeta, instance);
    }

    private void registryService(ServiceMeta serviceMeta) {
        rc.register(serviceMeta, instance);
    }

    private void genInterface(Object x) {
        String version = x.getClass().getAnnotation(HbProvider.class).version();
        Arrays.stream(x.getClass().getInterfaces()).forEach(
                itfer -> {
                    Method[] methods = itfer.getMethods();
                    for (Method method : methods) {
                        if (MethodUtils.checkLocalMethod(method)) {
                            continue;
                        }
                        createProvider(itfer, x, method, version);
                    }
                });
    }

    private void createProvider(Class<?> itfer, Object x, Method method, String version) {
        ProviderMeta meta = ProviderMeta.builder().method(method).serviceImpl(x).methodSign(MethodUtils.methodSign(method))
                .build();
        System.out.println(" create a provider: " + meta);
        ServiceMeta serviceMeta = ServiceMeta.builder().app(app).namespace(namespace).env(env).name(itfer.getCanonicalName()).version(version)
                .build();
        skeleton.add(serviceMeta, meta);
    }



}
