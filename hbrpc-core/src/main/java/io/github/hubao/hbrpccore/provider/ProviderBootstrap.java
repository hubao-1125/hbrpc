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

    // 启动项目初始化实现类
    @PostConstruct
    public void buildProviders() {

        // 获取使用@HbProvider注解的类数据
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(HbProvider.class);

        providers.forEach((k, v) -> System.out.println(k));
        // 循环处理
        providers.values().forEach(this::getInterface);

    }

    private void getInterface(Object i) {

        // 获取数据，补充到全局 Map
        Class<?> anInterface = i.getClass().getInterfaces()[0];
        skeleton.put(anInterface.getCanonicalName(), i);
    }

    public RpcResponse invoke(RpcRequest request) {

        // 根据 rpcRequest获取对应实现类
        Object bean = skeleton.get(request.getService());
        try {
            // 获取方法
            Method method = findMethod(bean.getClass(), request.getMethod());
            // 反射
            Object result = method.invoke(bean, request.getArgs());
            return new RpcResponse(true, result);
        } catch (InvocationTargetException | IllegalAccessException e) {
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
