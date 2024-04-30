package io.github.hubao.hbrpc.core.api;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

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

    private Map<String,String> params = new HashMap<>();
}
