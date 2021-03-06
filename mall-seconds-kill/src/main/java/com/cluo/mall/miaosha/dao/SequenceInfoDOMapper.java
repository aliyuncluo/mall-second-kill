package com.cluo.mall.miaosha.dao;

import org.apache.ibatis.annotations.Mapper;

import com.cluo.mall.miaosha.entity.SequenceInfoDO;

@Mapper
public interface SequenceInfoDOMapper {
	
	SequenceInfoDO getSequenceByName(String name);
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int deleteByPrimaryKey(String name);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int insert(SequenceInfoDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int insertSelective(SequenceInfoDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    SequenceInfoDO selectByPrimaryKey(String name);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int updateByPrimaryKeySelective(SequenceInfoDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sequence_info
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int updateByPrimaryKey(SequenceInfoDO record);
}