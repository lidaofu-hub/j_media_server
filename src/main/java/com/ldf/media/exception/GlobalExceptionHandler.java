package com.ldf.media.exception;


import cn.hutool.core.util.StrUtil;
import com.ldf.media.api.model.Result;
import com.ldf.media.enums.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;

/**
 * 全局异常处理
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 默认全局异常处理
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception ex, HttpServletRequest request) {
        log.error("【业务】接口异常 接口接口地址：{} 错误信息：{}", request.getRequestURI(), ex.getMessage(), ex);
        return new Result<>(ResultEnum.ERROR);
    }

    /**
     * 接口缺少参数
     *
     * @param ex
     * @param request
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Result<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, HttpServletResponse response, HttpServletRequest request) {
        String parameterName = ex.getParameterName();
        String parameterType = ex.getParameterType();
        String format = StrUtil.format("请求的接口缺少参数类型为：{} 参数名为：{} 的参数", parameterType, parameterName);
        log.error("【业务】接口缺少参数异常 接口接口地址：{} 错误信息：{}", request.getRequestURI(), format);
        return new Result<>(ResultEnum.ERROR.getCode(), format);
    }

    /**
     * 接口参数类型错误
     *
     * @param ex
     * @param request
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public Result<String> handleMethodArgumentTypeMismatchExceptions(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String name = ex.getName();
        String typename = ex.getRequiredType().getName();
        Object value = ex.getValue();
        String format = StrUtil.format("请求的接口需要的参数类型为：{} 参数名为：{} 的参数 错误值为 ：{}", typename, name, value.toString());
        log.error("【业务】接口参数类型不匹配异常 接口接口地址：{} 错误信息：{}", request.getRequestURI(), format);
        return new Result<>(ResultEnum.ERROR.getCode(), format);
    }


    /**
     * Json参数异常
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Result<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.error("【业务】接口Json参数异常 接口接口地址：{} 错误信息：{}", request.getRequestURI(), ex.getMessage(), ex);
        return new Result<>(ResultEnum.ERROR.getCode(), "JSON对象中的参数类型不正确");
    }

    /**
     * 一般的参数绑定时候抛出的异常
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<String> handleBindException(BindException ex, HttpServletRequest request) {
        log.error("【业务】接口Json绑定异常 接口接口地址：{} 错误信息：{}", request.getRequestURI(), ex.getMessage(), ex);
        return new Result<>(ResultEnum.ERROR.getCode(), "JSON对象中的参数数据不符合规范或绑定异常");
    }

    /**
     * json参数绑定
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        BindingResult bindingResult = ex.getBindingResult();
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        log.error("【业务】接口Json参数绑定异常 接口地址：{} 错误信息：{}", request.getRequestURI(), allErrors.get(0).getDefaultMessage(), ex);
        return new Result<>(ResultEnum.ERROR.getCode(), allErrors.get(0).getDefaultMessage());
    }

    /**
     * 接口参数校验异常
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(value = ConstraintViolationException.class)
    public Result<String> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        for (ConstraintViolation constraintViolation : ex.getConstraintViolations()) {
            //只提示第一个
            log.error("【业务】接口参数校验异常 接口地址：{} 错误信息：{}", request.getRequestURI(), constraintViolation.getMessage(), ex);
            return new Result<>(ResultEnum.ERROR.getCode(), constraintViolation.getMessage());
        }
        return new Result<>(ResultEnum.ERROR.getCode(), "参数为空或错误");
    }


    /**
     * 接口访问方法不支持
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<String> handleHttpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException ex) {
        String format = StrUtil.format("请求方法不支持! 当前方法请求方式为：{} 支持的请求方法为：{}", ex.getMethod(), ex.getSupportedMethods()[0], ex);
        log.error("【系统】接口方法错误异常 接口地址：{} 错误信息：{}", request.getRequestURI(), format);
        return new Result<>(ResultEnum.ERROR.getCode(), format);
    }


    /**
     * 全局断言异常
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.error("【业务】业务断言异常异常 接口地址：{} 错误信息：{}", request.getRequestURI(), ex.getMessage());
        return new Result<>(500, ex.getMessage());
    }

}
