package com.miaoshaProject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaProject.controller.viewobject.UserVO;
import com.miaoshaProject.error.BusinessException;
import com.miaoshaProject.error.EmBusinessError;
import com.miaoshaProject.response.CommonReturnType;
import com.miaoshaProject.service.UserService;
import com.miaoshaProject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class UserController extends BaseController {
    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @RequestMapping(value = "/login" , method ={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public  CommonReturnType login(@RequestParam(name = "telphone")String telphone,
                                   @RequestParam(name = "password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if(org.apache.commons.lang3.StringUtils.isEmpty(telphone) ||
        StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //用户登陆是否合法
        UserModel userModel=userService.validateLogin(telphone,this.EncodeByMd5(password));

        //登陆凭证加入用户成功的session
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        return CommonReturnType.create(null);
    }


    @RequestMapping(value = "/register" , method ={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    //用户注册接口
    public CommonReturnType register(@RequestParam(name="telphone")String telphone,
                                     @RequestParam(name="otpCode")String otpCode,
                                     @RequestParam(name="name")String name,
                                     @RequestParam(name="gender")Integer gender,
                                     @RequestParam(name="age")Integer age,
                                     @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
//        验证手机号和短信码符合
        String inSessionOptCode=(String)this.httpServletRequest.getSession().getAttribute(telphone);
        if(!StringUtils.equals(otpCode,inSessionOptCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"短信验证码不符");
        }
        //用户注册
        UserModel userModel=new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64en=new BASE64Encoder();
        //加密
        String newstr=base64en.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

    @RequestMapping(value = "/getOtp" , method ={RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    //用户获取短信的OTP
    public CommonReturnType getOtp(@RequestParam(name="telphone")String telphone){
        //按照一定规则生成OTP验证码
        Random random=new Random();
        int randomInt=random.nextInt(99999);
        randomInt+=10000;
        String otpCode=String.valueOf(randomInt);

        //OTP关联用户手机号 Redis HTTPSession绑定
        httpServletRequest.getSession().setAttribute(telphone,otpCode);

        //OTP通过短信发送给用户
        System.out.println("telphone"+telphone+"& optCode"+otpCode);

        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name="id")Integer id) throws BusinessException {
        //调用service服务获取对应用户的id对象返回给前端
        UserModel userModel=userService.getUserById(id);

        if(userModel==null){
            userModel.setEncrptPassword("123");
//            throw new BusinessException(EmBusinessError.USER_NOT_EXSITS);
        }
        //后端的对象转换为前端的对象
        UserVO userVo=convertFromModel(userModel);
        return CommonReturnType.create(userVo);
    }

    public UserVO convertFromModel(UserModel userModel){
        if(userModel==null)
            return null;
        UserVO userVo=new UserVO();
        BeanUtils.copyProperties(userModel,userVo);
        return userVo;
    }

}
