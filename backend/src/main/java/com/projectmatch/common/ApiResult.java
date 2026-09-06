package com.projectmatch.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<T>(0, "ok", data);
    }

    public static <T> ApiResult<T> ok(String message, T data) {
        return new ApiResult<T>(0, message, data);
    }

    public static <T> ApiResult<T> fail(String message) {
        return new ApiResult<T>(1, message, null);
    }

    public static <T> ApiResult<T> fail(int code, String message) {
        return new ApiResult<T>(code, message, null);
    }
}
