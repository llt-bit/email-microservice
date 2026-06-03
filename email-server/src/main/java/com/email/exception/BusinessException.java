package com.email.exception;

import lombok.Getter;

/**
 * 业务异常（替代 OA 的 BusinessException）。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
