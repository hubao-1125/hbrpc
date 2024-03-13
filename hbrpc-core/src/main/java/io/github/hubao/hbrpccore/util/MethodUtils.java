package io.github.hubao.hbrpccore.util;

import java.lang.reflect.Method;

/**
 * Description for this class.
 *
 * @Author : kimmking(kimmking@apache.org)
 * @create 2024/3/10 21:49
 */
public class MethodUtils {

    public static boolean checkLocalMethod(final String methodName) {
        //本地方法不代理
        if ("toString".equals(methodName) ||
                "hashCode".equals(methodName) ||
                "notifyAll".equals(methodName) ||
                "equals".equals(methodName) ||
                "wait".equals(methodName) ||
                "getClass".equals(methodName) ||
                "notify".equals(methodName)) {
            return true;
        }
        return false;
    }


    // 等价上面的
    public static boolean checkLocalMethod(final Method method) {
        return method.getDeclaringClass().equals(Object.class);
    }

}
