package io.github.hubao.hbrpccore.consumer;

import io.github.hubao.hbrpccore.annotation.HbConsumer;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;

@Data
public class ConsumerBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> stub = new HashMap<>();

    public void start() {
        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            Object bean = applicationContext.getBean(name);
            List<Field> fields = findAnnotationField(bean.getClass());

            fields.stream().forEach(i->{

                try {
                    Class<?> service = i.getType();
                    String serviceName = service.getCanonicalName();
                    Object consumer = stub.get(serviceName);
                    if (Objects.isNull(consumer)) {
                        consumer = createConsumer(service);
                    }

                    i.setAccessible(true);
                    i.set(bean, consumer);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });

        }
    }

    private Object createConsumer(Class<?> service) {

        return Proxy.newProxyInstance(service.getClassLoader(),
        new Class[]{service}, new HbInvocationHandler(service));
    }

    private List<Field> findAnnotationField(Class<?> aClass) {

        List<Field> result = new ArrayList<>();

        while (aClass != null) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(HbConsumer.class)) {
                    result.add(field);
                }
            }
            aClass = aClass.getSuperclass();
        }


        return result;
    }
}
