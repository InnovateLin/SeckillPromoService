package com.miaoshaProject.error;



//包装器模式
public class BusinessException extends Exception implements CommonError {
    private CommonError commonError;

    //直接接收EmBusinessExceptionError的传参用于构造业务异常
    public BusinessException(CommonError commonError){
        super();
        this.commonError=commonError;
    }

    //接收自定义的errMsg的方式构造业务异常
    public BusinessException(CommonError commonError,String errMsg){
        super();
        this.commonError=commonError;
        this.commonError.setErrMsg(errMsg);
    }


    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getErrMessage() {
        return this.commonError.getErrMessage();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.commonError.setErrMsg(errMsg);
        return this;
    }

    public CommonError getCommonError() {
        return commonError;
    }
}
