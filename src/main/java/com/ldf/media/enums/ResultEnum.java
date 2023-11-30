package com.ldf.media.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回枚举
 *
 * @author lidaofu
 * @since 2023/4/4
 **/
@Getter
@AllArgsConstructor
public enum ResultEnum {
    /**
     * 请求成功
     */
    SUCCESS(0, "请求成功"),

    /**
     * 请求失败
     */
    ERROR(-1, "请求成功"),
    /**
     * 未登录
     */
    NOT_LOGIN(-100, "未提供有效凭据"),

    /**
     * 无权限
     */
    NOT_POWER(-100, "无权限"),

    /**
     * 提交限制
     */
    INVALID_ARGS (-300, "请求过快"),

    /**
     * 请求失败
     */
    EXCEPTION(-400, "请求或操作失败"),


    ;

    /**
     * 代码
     */
    private final Integer code;

    /**
     * 信息
     */
    private final String msg;
}
