package io.github.hubao.hbrpccore.consumer;

import io.github.hubao.hbrpccore.annotation.HbConsumer;
import io.github.hubao.hbrpccore.api.LoadBalancer;
import io.github.hubao.hbrpccore.api.RegistryCenter;
import io.github.hubao.hbrpccore.api.Router;
import io.github.hubao.hbrpccore.api.RpcContext;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;

@Data
public class ConsumerBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    @Autowired
    Environment environment;

    private Map<String, Object> stub = new HashMap<>();

    public void start() {

        Router router = applicationContext.getBean(Router.class);
        LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);

        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);

        RpcContext rpcContext = new RpcContext();
        rpcContext.setRouter(router);
        rpcContext.setLoadBalancer(loadBalancer);

//        String urls = environment.getProperty("hbrpc.providers", "");
//        if (Strings.isEmpty(urls)) {
//            System.out.println("hbrpc:providers");
//        }
//        String[] providers = urls.split(",");


        String[] names = applicationContext.getBeanDefinitionNames();
        long start = System.currentTimeMillis();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);

            // 优化点1：过滤去掉spring/jdk/其他框架本身的bean的反射扫描 TODO 1
            String packageName = bean.getClass().getPackageName();
            if (packageName.startsWith("org.springframework") ||
                    packageName.startsWith("java.") ||
                    packageName.startsWith("javax.") ||
                    packageName.startsWith("jdk.") ||
                    packageName.startsWith("com.fasterxml.") ||
                    packageName.startsWith("com.sun.") ||
                    packageName.startsWith("jakarta.") ||
                    packageName.startsWith("org.apache") ) {
                continue;  // 这段逻辑可以降低一半启动速度 300ms->160ms
            }
            System.out.println(packageName + " package bean => " + name);

            List<Field> fields = findAnnotatedField(bean.getClass());

            fields.stream().forEach( f -> {
                System.out.println(" ===> " + f.getName());
                try {
                    Class<?> service = f.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
                        consumer = createConsumerFromRegistry(service, rpcContext, rc);
//                        consumer = createConsumer(service, rpcContext, List.of(providers));
                    }
                    f.setAccessible(true);
                    f.set(bean, consumer);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

        }
        System.out.println("create consumer take " + (System.currentTimeMillis()-start) + " ms");
    }

    private Object createConsumerFromRegistry(Class<?> service, RpcContext rpcContext, RegistryCenter rc) {

        String serviceName = service.getCanonicalName();
        List<String> providers = rc.fetchAll(serviceName);

        return createConsumer(service, rpcContext, providers);
    }

    private Object createConsumer(Class<?> service, RpcContext rpcContext, List<String> providers) {
        return Proxy.newProxyInstance(service.getClassLoader(),
                new Class[]{service}, new HbInvocationHandler(service, rpcContext, providers));
    }

    private List<Field> findAnnotatedField(Class<?> aClass) {
        List<Field> result = new ArrayList<>();
        while (aClass != null) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field f : fields) {
                if (f.isAnnotationPresent(HbConsumer.class)) {
                    result.add(f);
                }
            }
            aClass = aClass.getSuperclass();
        }
        return result;
    }

}
