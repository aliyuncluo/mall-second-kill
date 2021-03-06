package com.cluo.mall.miaosha.dao;

import org.apache.ibatis.annotations.Mapper;

import com.cluo.mall.miaosha.entity.UserInfoDO;

@Mapper
public interface UserInfoDOMapper {
	
	UserInfoDO selectByTelphone(String telphone);
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Fri Jul 31 18:26:46 CST 2020
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Fri Jul 31 18:26:46 CST 2020
     */
    int insert(UserInfoDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Fri Jul 31 18:26:46 CST 2020
     */
    int insertSelective(UserInfoDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Fri Jul 31 18:26:46 CST 2020
     */
    UserInfoDO selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Fri Jul 31 18:26:46 CST 2020
     */
    int updateByPrimaryKeySelective(UserInfoDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user_info
     *
     * @mbg.generated Fri Jul 31 18:26:46 CST 2020
     */
    int updateByPrimaryKey(UserInfoDO record);
}