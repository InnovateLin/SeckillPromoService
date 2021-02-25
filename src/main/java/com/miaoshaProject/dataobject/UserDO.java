package com.miaoshaProject.dataobject;

public class UserDO {

    private Integer id;

    private String name;


    private Byte gender;


    private Integer age;


    private String telephone;

    private String registerMode;

    private String thirdPartyId;


    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }


    public Byte getGender() {
        return gender;
    }


    public void setGender(Byte gender) {
        this.gender = gender;
    }


    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }


    public String getTelephone() {
        return telephone;
    }


    public void setTelephone(String telephone) {
        this.telephone = telephone == null ? null : telephone.trim();
    }


    public String getRegisterMode() {
        return registerMode;
    }


    public void setRegisterMode(String registerMode) {
        this.registerMode = registerMode == null ? null : registerMode.trim();
    }


    public String getThirdPartyId() {
        return thirdPartyId;
    }


    public void setThirdPartyId(String thirdPartyId) {
        this.thirdPartyId = thirdPartyId == null ? null : thirdPartyId.trim();
    }
}