package com.blueorbit.teamup.controller;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class,
            BindException.class,
            HttpMessageNotReadableException.class
    })
    public Result handleBadRequest(Exception ignored) {
        return new Result(Code.PARAM_ERR, null, Msg.PARAM_INVALID);
    }

    @ExceptionHandler(IllegalStateException.class)
    public Result handleBizException(IllegalStateException ex) {
        return new Result(Code.BIZ_ERR, null, ex.getMessage());
    }
}
