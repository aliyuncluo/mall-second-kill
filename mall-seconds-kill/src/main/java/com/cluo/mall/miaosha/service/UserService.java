package com.cluo.mall.miaosha.service;

import com.cluo.mall.miaosha.error.BusinessException;
import com.cluo.mall.miaosha.service.model.UserModel;

/**
 * Created by hzllb on 2018/11/11.
 */
public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;

    //通过缓存获取用户对象
    UserModel getUserByIdInCache(Integer id);
    /*
    telphone:用户注册手机
    password:用户加密后的密码
     */
    UserModel validateLogin(String telphone,String encrptPassword) throws BusinessException;
}
