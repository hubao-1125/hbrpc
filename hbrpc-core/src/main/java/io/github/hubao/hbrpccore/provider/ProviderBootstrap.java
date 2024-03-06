package io.github.hubao.hbrpccore.provider;

import io.github.hubao.hbrpccore.annotation.HbProvider;
import io.github.hubao.hbrpccore.api.RpcRequest;
import io.github.hubao.hbrpccore.api.RpcResponse;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Data
public class ProviderBootstrap implements ApplicationContextAware {

    ApplicationContext applicationContext;

    private Map<String, Object> skeleton = new HashMap<>();

    @PostConstruct
    public void buildProviders() {
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(HbProvider.class);

        providers.forEach((k, v) -> System.out.println(k));
        providers.values().forEach(i -> getInterface(i));

    }

    private void getInterface(Object i) {

        Class<?> anInterface = i.getClass().getInterfaces()[0];
        skeleton.put(anInterface.getCanonicalName(), i);
    }

    public RpcResponse invoke(RpcRequest request) {

        Object bean = skeleton.get(request.getService());
        try {
            Method method = findMethod(bean.getClass(), request.getMethod());
            Object result = method.invoke(bean, request.getArgs());
            return new RpcResponse(true, result);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Method findMethod(Class<?> aClass, String methodName) {

        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

}
