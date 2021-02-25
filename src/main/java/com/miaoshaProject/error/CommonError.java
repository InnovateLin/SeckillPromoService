package com.miaoshaProject.error;


public interface CommonError {
    public int getErrCode();
    public String getErrMessage();
    public CommonError setErrMsg(String errMsg);
}
