package com.cluo.mall.miaosha.dao;

import org.apache.ibatis.annotations.Mapper;

import com.cluo.mall.miaosha.entity.StockLogDO;

@Mapper
public interface StockLogDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int deleteByPrimaryKey(String stockLogId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int insert(StockLogDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int insertSelective(StockLogDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    StockLogDO selectByPrimaryKey(String stockLogId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int updateByPrimaryKeySelective(StockLogDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table stock_log
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int updateByPrimaryKey(StockLogDO record);
}