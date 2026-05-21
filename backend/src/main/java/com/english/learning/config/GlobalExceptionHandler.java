package com.english.learning.config;

import com.english.learning.common.Result;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthenticatedException.class)
    public Result<?> handleUnauthenticated(UnauthenticatedException e) {
        return Result.error(401, "未登录或登录已过期");
    }

    @ExceptionHandler(AuthorizationException.class)
    public Result<?> handleAuthorization(AuthorizationException e) {
        return Result.error(403, "无权访问");
    }
}
