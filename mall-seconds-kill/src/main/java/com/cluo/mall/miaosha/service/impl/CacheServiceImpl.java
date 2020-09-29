package com.cluo.mall.miaosha.service.impl;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.cluo.mall.miaosha.service.CacheService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Service
public class CacheServiceImpl implements CacheService{
    //google cache
	private Cache<String, Object> commonCache=null;
	
	@PostConstruct
	public void init() {
		CacheBuilder.newBuilder().initialCapacity(10) //设置缓存容器的初始容量为10
		                         .maximumSize(100)   //设置缓存中最大可以存储100个KEY,超过100个后会按照LRU的策略移除缓存项
		                         .expireAfterWrite(60, TimeUnit.SECONDS) //设置写缓存后多少秒过期
		                         .build();
	}
	
	//写数据
	@Override
	public void setCommonCache(String key, Object value) {
		// TODO Auto-generated method stub
		commonCache.put(key, value);
	}
  
	//读数据
	@Override
	public Object getFromCommonCache(String key) {
		// TODO Auto-generated method stub
		return commonCache.getIfPresent(key);
	}

}
