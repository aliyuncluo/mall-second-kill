package com.cluo.mall.miaosha.service.impl;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.cluo.mall.miaosha.dao.PromoDOMapper;
import com.cluo.mall.miaosha.entity.PromoDO;
import com.cluo.mall.miaosha.error.BusinessException;
import com.cluo.mall.miaosha.error.EmBusinessError;
import com.cluo.mall.miaosha.service.ItemService;
import com.cluo.mall.miaosha.service.PromoService;
import com.cluo.mall.miaosha.service.UserService;
import com.cluo.mall.miaosha.service.model.ItemModel;
import com.cluo.mall.miaosha.service.model.PromoModel;
import com.cluo.mall.miaosha.service.model.UserModel;

/**
 * Created by hzllb on 2018/11/18.
 */
@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        //获取对应商品的秒杀活动信息
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        //dataobject->model
        PromoModel promoModel = convertFromDataObject(promoDO);
        if(promoModel == null){
            return null;
        }

        //判断当前时间是否秒杀活动即将开始或正在进行
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }
        return promoModel;
    }
    private PromoModel convertFromDataObject(PromoDO promoDO){
        if(promoDO == null){
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        return promoModel;
    }
    
    /**
     * @desc 活动发布
     */
	@Override
	public void publishPromo(Integer promoId) {
		// 通过ID获取活动
		PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
		if(promoDO.getItemId()==null || promoDO.getItemId().intValue()==0) {
			return;
		}
		ItemModel itemModel = itemService.getItemById(promoDO.getItemId());
		
		//发布活动时，将库存同步到redis
		redisTemplate.opsForValue().set("promo_item_stock_"+itemModel.getId(), itemModel.getStock());
		
		//将秒杀大闸的限制数字设到redis内 库存的5倍
		redisTemplate.opsForValue().set("promo_door_count_"+promoId, itemModel.getStock().intValue()*5);
	}
	
	
	@Override
	public String generateSecondKillToken(Integer promoId,Integer itemId,Integer userId) {
		//判断库存已售罄,若key存在直接返回下单失败
        boolean hasKey = redisTemplate.hasKey("promo_item_stock_invalid_"+itemId);
        if(hasKey) {
        	return null;
        }
		
		//获取对应商品的秒杀活动信息
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);

        //dataobject->model
        PromoModel promoModel = convertFromDataObject(promoDO);
        if(promoModel == null){
            return null;
        }

        //判断当前时间是否秒杀活动即将开始或正在进行
        if(promoModel.getStartDate().isAfterNow()){
            promoModel.setStatus(1);
        }else if(promoModel.getEndDate().isBeforeNow()){
            promoModel.setStatus(3);
        }else{
            promoModel.setStatus(2);
        }
        //判断活动是否正在进行中  1表示还未开始，2表示进行中，3表示已结束
        if(promoModel.getStatus().intValue()!=2) {
        	return null;
        }
        //判断item是否存在
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if(itemModel == null){
            return null;
        }
        //判断用户是否存在
        UserModel userModel = userService.getUserByIdInCache(userId);
        if(userModel == null){
            return null;
        }
        
        //获取秒杀大闸count的数量 ，减少1
        long result = redisTemplate.opsForValue().increment("promo_door_count_"+promoId,-1);
        if(result<0) { //不能拿到token，即不能参与秒杀
        	return null;
        }
        
        //生成token并且存入redis内，给一个5分钟的有效期
        String token = UUID.randomUUID().toString().replace("-", "");
        //保存到redis
        final String promoRedisKey = "promo_token_"+promoId+"_userid_"+userId+"_itemid_"+itemId;
        redisTemplate.opsForValue().set(promoRedisKey, token);
        redisTemplate.expire(promoRedisKey, 5, TimeUnit.MINUTES);
        
        return token;
	}
}
