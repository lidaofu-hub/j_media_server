package com.ldf.media.api.model.result;

import com.ldf.media.enums.ResultEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 返回类
 *
 * @author lidaofu
 * @since 2023/4/10
 **/
@Data
@AllArgsConstructor
public class Result<T> {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 信息
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

    public Result() {
        this.code = ResultEnum.SUCCESS.getCode();
        this.msg = ResultEnum.SUCCESS.getMsg();
    }

    public Result(T t) {
        this.code = ResultEnum.SUCCESS.getCode();
        this.msg = ResultEnum.SUCCESS.getMsg();
        this.data = t;
    }

    public Result(ResultEnum resultEnum) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getMsg();
    }

    public Result(ResultEnum resultEnum, T t) {
        this.code = resultEnum.getCode();
        this.msg = resultEnum.getMsg();
        this.data = t;
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
