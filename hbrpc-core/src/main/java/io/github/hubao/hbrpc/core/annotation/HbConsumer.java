package io.github.hubao.hbrpc.core.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface HbConsumer {

    String version() default "";
}
