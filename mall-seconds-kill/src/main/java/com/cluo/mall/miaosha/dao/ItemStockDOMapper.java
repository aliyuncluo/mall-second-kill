package com.cluo.mall.miaosha.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cluo.mall.miaosha.entity.ItemStockDO;

@Mapper
public interface ItemStockDOMapper {
	
	ItemStockDO selectByItemId(Integer itemId);
	
	int decreaseStock(@Param("itemId") Integer itemId, @Param("amount") Integer amount);
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int insert(ItemStockDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int insertSelective(ItemStockDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    ItemStockDO selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int updateByPrimaryKeySelective(ItemStockDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item_stock
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int updateByPrimaryKey(ItemStockDO record);
}