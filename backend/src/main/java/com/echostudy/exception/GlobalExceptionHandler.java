package com.echostudy.exception;

import com.echostudy.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        return Result.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleBindException(Exception ex) {
        return Result.fail(400, "请填写完整且有效的信息");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException ex) {
        return Result.fail(400, "请检查填写内容是否符合要求");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation", ex);
        return Result.fail(400, resolveDataIntegrityMessage(ex));
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return Result.fail(500, "操作失败，请稍后重试或联系管理员");
    }

    private String resolveDataIntegrityMessage(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() == null ? ex.getMessage() : ex.getMostSpecificCause().getMessage();
        String normalized = message == null ? "" : message.toLowerCase();
        if (normalized.contains("time_node") && isDuplicateMessage(normalized)) {
            return "该节点已存在";
        }
        if (isDuplicateMessage(normalized)) {
            return "该数据已存在，请勿重复添加";
        }
        if (normalized.contains("foreign key") || normalized.contains("reference") || normalized.contains("冲突")) {
            return "该数据正在被使用，不能直接删除或修改";
        }
        return "当前操作不符合数据规则，请检查填写内容";
    }

    private boolean isDuplicateMessage(String message) {
        return message.contains("unique") || message.contains("duplicate") || message.contains("重复");
    }
}
