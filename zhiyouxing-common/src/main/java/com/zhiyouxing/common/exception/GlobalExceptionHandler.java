package com.zhiyouxing.common.exception;

import com.zhiyouxing.common.result.Result;
import com.zhiyouxing.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.build(e.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        return Result.build(ResultCode.VALIDATE_FAILED.getCode(), firstErrorMessage(e.getBindingResult().getFieldError()), null);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        return Result.build(ResultCode.VALIDATE_FAILED.getCode(), firstErrorMessage(e.getBindingResult().getFieldError()), null);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.failed(ResultCode.FAILED);
    }

    private String firstErrorMessage(FieldError fieldError) {
        return fieldError == null ? ResultCode.VALIDATE_FAILED.getMessage() : fieldError.getDefaultMessage();
    }
}
