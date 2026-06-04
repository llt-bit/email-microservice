package com.email.exception;

import com.email.common.R;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理 —— 将异常转为统一 JSON 响应。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: " + e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ":" + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("参数校验失败");
        return R.fail(400, msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return R.fail(400, "缺少必填参数: " + e.getParameterName());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleUploadSize(MaxUploadSizeExceededException e) {
        return R.fail(413, "上传文件大小超过限制");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleForbidden(AccessDeniedException e) {
        return R.fail(403, "无访问权限");
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleDb(DataAccessException e) {
        log.error("数据库异常", e);
        return R.fail(500, "数据操作失败，请稍后重试");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleRuntime(RuntimeException e) {
        log.error("未知运行异常", e);
        return R.fail(500, "系统内部错误: " + (e.getMessage() != null ? e.getMessage() : "未知错误"));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public R<Void> handleAll(Exception e) {
        log.error("未捕获异常", e);
        return R.fail(500, "系统内部错误");
    }
}
