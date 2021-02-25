package com.miaoshaProject.controller;

import com.miaoshaProject.error.BusinessException;
import com.miaoshaProject.error.EmBusinessError;
import com.miaoshaProject.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {

    public static final String CONTENT_TYPE_FORMED="application/x-www-form-urlencoded";

    //定义ExceptionHandler解决没被controller层解决的异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Object handleException(HttpServletRequest request, Exception ex) {
        Map<String,Object> responseData=new HashMap<>();

        if(ex instanceof BusinessException){
            BusinessException businessException=(BusinessException)ex;
            responseData.put("errCode",businessException.getErrCode());
            responseData.put("errMsg",businessException.getErrMessage());
        }else{
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg",EmBusinessError.UNKNOWN_ERROR.getErrMessage());
        }
        return CommonReturnType.create(responseData,"fail");
    }
}
