package io.github.hubao.hbrpc.core.annotation;

import io.github.hubao.hbrpc.core.config.ProviderConfig;
import io.github.hubao.hbrpc.core.consumer.ConsumerConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class})
public @interface EnableHbRPC {
}
