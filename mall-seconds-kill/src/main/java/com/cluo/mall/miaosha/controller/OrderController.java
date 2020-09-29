package com.cluo.mall.miaosha.controller;

import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cluo.mall.miaosha.error.BusinessException;
import com.cluo.mall.miaosha.error.EmBusinessError;
import com.cluo.mall.miaosha.mq.MqProducer;
import com.cluo.mall.miaosha.response.CommonReturnType;
import com.cluo.mall.miaosha.service.ItemService;
import com.cluo.mall.miaosha.service.OrderService;
import com.cluo.mall.miaosha.service.PromoService;
import com.cluo.mall.miaosha.service.model.OrderModel;
import com.cluo.mall.miaosha.service.model.UserModel;
import com.cluo.mall.miaosha.util.CodeUtil;
import com.google.common.util.concurrent.RateLimiter;

/**
 * Created by hzllb on 2018/11/18.
 */
@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseController {
	private Logger logger = LoggerFactory.getLogger(OrderController.class);
	
    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemService itemService;
    
    @Autowired
    private HttpServletRequest httpServletRequest;
    
    @Autowired
    private MqProducer mqProducer;
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Autowired
    private PromoService promoService;
    
    //用于做队列泄洪
    private ExecutorService executorService;
    
    //google guava 用于流量限流
    private RateLimiter orderCreateRateLimiter;
    
    
    @PostConstruct
    public void init() {
    	executorService = Executors.newFixedThreadPool(20);
    	orderCreateRateLimiter = RateLimiter.create(300);
    }
    
    //生成验证码
    @RequestMapping(value = "/generateVerifyCode",method = {RequestMethod.POST})
    @ResponseBody
    public void generateVerifyCode(HttpServletResponse response) throws BusinessException, Exception {
    	//获取token
    	String token = httpServletRequest.getParameterMap().get("token")[0];
    	if(StringUtils.isBlank(token)) {
    		throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能生成验证码");
    	}
    	//获取用户登陆信息
    	UserModel userModel = (UserModel)redisTemplate.opsForValue().get(token);
    	if(userModel==null) {
    		throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
    	}
    	Map<String, Object> map = CodeUtil.generateCodeAndPic();
    	redisTemplate.opsForValue().set("verify_code_"+userModel.getId(), map.get("code"));
    	redisTemplate.expire("verify_code_"+userModel.getId(), 10, TimeUnit.MINUTES);
        //生成验证码图片
    	ImageIO.write((RenderedImage)map.get("codePic"), "jpeg", response.getOutputStream());
    	logger.info("验证码的值为：{}",map.get("code"));
    }

    //生成秒杀的令牌
    @RequestMapping(value = "/generateToken",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType generateToken(@RequestParam(name="itemId")Integer itemId,
                                         @RequestParam(name="promoId")Integer promoId,
                                         @RequestParam(name="verifyCode")String verifyCode) throws BusinessException {
    	
    	//获取token
    	String token = httpServletRequest.getParameterMap().get("token")[0];
    	if(StringUtils.isBlank(token)) {
    		throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
    	}
    	//获取用户登陆信息
    	UserModel userModel = (UserModel)redisTemplate.opsForValue().get(token);
    	if(userModel==null) {
    		throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
    	}
    	//从redis中获取验证码
    	String redisVerifyCode = String.valueOf(redisTemplate.opsForValue().get("verify_code_"+userModel.getId()));
    	if(StringUtils.isEmpty(redisVerifyCode)) {
    		throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"请求非法");
    	}
    	if(!StringUtils.equalsIgnoreCase(verifyCode, redisVerifyCode)) {
    		throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"请求非法,验证码错误");
    	}
    	
    	//获取秒杀访问令牌
    	String promoToken = promoService.generateSecondKillToken(promoId, itemId, userModel.getId());
    	if(promoToken==null) {
    		throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"生成令牌失败");
    	}
    	//返回对应的结果
    	return CommonReturnType.create(promoToken);
    }
    
    
    //封装下单请求
    @RequestMapping(value = "/createorder",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name="itemId")Integer itemId,
                                        @RequestParam(name="amount")Integer amount,
                                        @RequestParam(name="promoId",required = false)Integer promoId,
                                        @RequestParam(name="promoToken",required = false)String promoToken) throws BusinessException {
    	//限流
    	if(orderCreateRateLimiter.acquire()<=0) {
    		throw new BusinessException(EmBusinessError.RATELIMIT);
    	}
    	
    	//获取token
    	String token = httpServletRequest.getParameterMap().get("token")[0];
    	if(StringUtils.isBlank(token)) {
    		throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
    	}

        //获取用户的登陆信息
        UserModel userModel = (UserModel)redisTemplate.opsForValue().get(token);
        if(userModel==null) {
        	throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户还未登陆，不能下单");
        }
        //校验秒杀令牌是否正确
    	if(promoId!=null) {
    		final String promoRedisKey = "promo_token_"+promoId+"_userid_"+userModel.getId()+"_itemid_"+itemId;
    		String redisPromoToken = String.valueOf(redisTemplate.opsForValue().get(promoRedisKey));
    		if(redisPromoToken==null) {
    			throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌校验失败");
    		}
    		if(!StringUtils.equals(promoToken, redisPromoToken)) { //前端传入的token与redis中取出的token进行比较
    			throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌校验失败");
    		}
    	}
    	
        //判断库存已售罄,若key存在直接返回下单失败
        boolean hasKey = redisTemplate.hasKey("promo_item_stock_invalid_"+itemId);
        if(hasKey) {
        	throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        
        //同步调用线程池的submit方法
        //拥塞窗口为20的等待队列，用来队列化泄洪
       Future<Object> future = executorService.submit(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				 //加入库存流水init状态
		        String stockLogId = itemService.initStockLog(itemId, amount);
		        
		        //OrderModel orderModel = orderService.createOrder(userModel.getId(),itemId,promoId,amount);
		        //redisTemplate.opsForValue().get("promo_item_stock_invalid_"+itemId);
		        
		        //完成对应的下单事务型消息机制
		        boolean mqResult = mqProducer.transactionAsyncReduceStock(userModel.getId(), promoId, itemId, amount);
		        if(!mqResult) {
		        	throw new BusinessException(EmBusinessError.UNKNOWN_ERROR,"下单失败");
		        }
		        return null;
			}
		});
        
       try {
		future.get();
	} catch (InterruptedException e) {
		throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
	} catch (ExecutionException e) {
		throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
	}
        return CommonReturnType.create(null);
    }
}
