package io.github.hubao.hbrpc.core.util;

import io.github.hubao.hbrpc.core.annotation.HbConsumer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FieldUtils {

    public static List<Field> findAnnotatedField(Class<?> aClass, Class<? extends Annotation> annoClass) {
        List<Field> result = new ArrayList<>();
        while (aClass != null) {
            Field[] fields = aClass.getDeclaredFields();
            for (Field f : fields) {
                if (f.isAnnotationPresent(annoClass)) {
                    result.add(f);
                }
            }
            aClass = aClass.getSuperclass();
        }
        return result;
    }
}
