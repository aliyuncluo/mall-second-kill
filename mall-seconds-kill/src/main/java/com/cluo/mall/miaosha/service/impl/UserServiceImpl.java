package com.cluo.mall.miaosha.service.impl;

import com.cluo.mall.miaosha.dao.UserInfoDOMapper;
import com.cluo.mall.miaosha.error.BusinessException;
import com.cluo.mall.miaosha.error.EmBusinessError;
import com.cluo.mall.miaosha.service.UserService;
import com.cluo.mall.miaosha.service.model.UserModel;
import com.cluo.mall.miaosha.validator.ValidatorImpl;
import com.cluo.mall.miaosha.validator.ValidationResult;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.cluo.mall.miaosha.dao.UserPasswordDOMapper;
import com.cluo.mall.miaosha.entity.UserInfoDO;
import com.cluo.mall.miaosha.entity.UserPasswordDO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by hzllb on 2018/11/11.
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;
    
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public UserModel getUserById(Integer id) {
        //调用userdomapper获取到对应的用户dataobject
        UserInfoDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO == null){
            return null;
        }
        //通过用户id获取对应的用户加密密码信息
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        return convertFromDataObject(userDO,userPasswordDO);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        ValidationResult result =  validator.validate(userModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }



        //实现model->dataobject方法
        UserInfoDO userDO = convertFromModel(userModel);
        try{
            userDOMapper.insertSelective(userDO);
        }catch(DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已重复注册");
        }



        userModel.setId(userDO.getId());

        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

        return;
    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        //通过用户的手机获取用户信息
        UserInfoDO userDO = userDOMapper.selectByTelphone(telphone);
        if(userDO == null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO,userPasswordDO);

        //比对用户信息内加密的密码是否和传输进来的密码相匹配
        if(!StringUtils.equals(encrptPassword,userModel.getEncrptPassword())){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }


    private UserPasswordDO convertPasswordFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }
    private UserInfoDO convertFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserInfoDO userDO = new UserInfoDO();
        BeanUtils.copyProperties(userModel,userDO);

        return userDO;
    }
    private UserModel convertFromDataObject(UserInfoDO userDO, UserPasswordDO userPasswordDO){
        if(userDO == null){
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);

        if(userPasswordDO != null){
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }

        return userModel;
    }

	@Override
	public UserModel getUserByIdInCache(Integer id) {
		
		UserModel userModel = (UserModel)redisTemplate.opsForValue().get("user_validate_"+id);
		if(userModel==null) {
			userModel = this.getUserById(id);
			redisTemplate.opsForValue().set("user_validate_"+id, userModel);
			redisTemplate.expire("user_validate_"+id, 10, TimeUnit.MINUTES);
		}
		return null;
	}
}
