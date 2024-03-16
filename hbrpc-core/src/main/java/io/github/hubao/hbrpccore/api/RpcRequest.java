package io.github.hubao.hbrpccore.api;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RpcRequest {

    /**
     * 接口
     */
    private String service;

    /**
     * 方法
     */
    private String methodSign;

    /**
     * 参数
     */
    private Object[] args;


}
