package com.atguigu.common.exception;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/9/28 6:13 下午
 * @Version 1.0
 */
public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验异常");

    private int code;
    private String message;

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
