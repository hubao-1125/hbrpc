package io.github.hubao.hbrpc.core.meta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * 功能描述: provider元数据
 * @author hubao
 * @Date: 2024/3/20 21:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProviderMeta {

    Method method;
    String methodSign;
    Object serviceImpl;

}
