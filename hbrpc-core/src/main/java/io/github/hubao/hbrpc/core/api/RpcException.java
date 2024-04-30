package io.github.hubao.hbrpc.core.api;

import lombok.Data;

@Data
public class RpcException extends RuntimeException{

    private String errorCode;

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public RpcException(Throwable cause, String errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public RpcException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public RpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


    public static final String SOCKET_TIMEOUT_EX = "X001-http_invoke_timeout";
    public static final String NO_SUCH_METHOD_EX = "X002-method_not_exists";
    public static final String ExceedLimitEx  = "X003" + "-" + "tps_exceed_limit";
    public static final String UNKNOWN_EX = "Z001-unknown";
}
