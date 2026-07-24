package com.smartparking.common;

import java.io.Serializable;

/**
 * 统一响应结果
 */
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;

    private Result() {}

    public static <T> Result<T> ok() {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "操作成功";
        return r;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = ok();
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok(String message, T data) {
        Result<T> r = ok(data);
        r.message = message;
        return r;
    }

    public static <T> Result<T> fail(String message) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.message = message;
        return r;
    }

    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> Result<T> error(Integer code, String message) {
        return fail(code, message);
    }

    // ========== Getters & Setters ==========
    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
