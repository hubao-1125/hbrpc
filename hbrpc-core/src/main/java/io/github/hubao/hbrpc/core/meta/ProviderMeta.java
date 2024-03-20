package io.github.hubao.hbrpc.core.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProviderMeta {

    Method method;
    String methodSign;
    Object serviceImpl;

}
