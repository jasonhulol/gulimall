package com.bootstudy.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/9/28 5:59 下午
 * @Version 1.0
 */
@Slf4j
//@ResponseBody
//@ControllerAdvice(basePackages = "com.bootstudy.gulimall.product.controller")
@RestControllerAdvice(basePackages = "com.bootstudy.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现问题{}，异常类型:{}", e.getMessage(),e.getClass());
        BindingResult result = e.getBindingResult();
        Map<String,String> map = new HashMap<>();
        result.getFieldErrors().forEach((fieldError -> {
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
        }));
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(),BizCodeEnum.VALID_EXCEPTION.getMessage()).put("data", map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMessage())
;    }


}
