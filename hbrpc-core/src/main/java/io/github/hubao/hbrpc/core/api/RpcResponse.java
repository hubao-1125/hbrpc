package io.github.hubao.hbrpc.core.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse<T> {

    /**
     * 状态
     */
    boolean status;

    /**
     * 返回数据
     */
    T data;

    Exception ex;
}
