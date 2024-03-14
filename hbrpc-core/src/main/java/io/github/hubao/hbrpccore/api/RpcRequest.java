package io.github.hubao.hbrpccore.api;

import lombok.Data;

@Data
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
