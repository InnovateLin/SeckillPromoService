package com.miaoshaProject.dataobject;

public class UserPasswordDO {

    private Integer id;


    private String encryptPassword;


    private Integer userId;


    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public String getEncryptPassword() {
        return encryptPassword;
    }


    public void setEncryptPassword(String encryptPassword) {
        this.encryptPassword = encryptPassword == null ? null : encryptPassword.trim();
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}