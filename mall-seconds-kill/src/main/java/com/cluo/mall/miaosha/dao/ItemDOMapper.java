package com.cluo.mall.miaosha.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.cluo.mall.miaosha.entity.ItemDO;

@Mapper
public interface ItemDOMapper {
	
	List<ItemDO> listItem();
	
	int increaseSales(@Param("id")Integer id,@Param("amount")Integer amount);
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int insert(ItemDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int insertSelective(ItemDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    ItemDO selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int updateByPrimaryKeySelective(ItemDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Fri Jul 31 18:26:45 CST 2020
     */
    int updateByPrimaryKey(ItemDO record);
}