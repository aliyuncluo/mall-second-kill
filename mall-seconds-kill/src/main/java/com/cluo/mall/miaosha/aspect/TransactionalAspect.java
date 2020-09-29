package com.cluo.mall.miaosha.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

@Component
@Aspect
public class TransactionalAspect {
    private Logger logger = LoggerFactory.getLogger(TransactionalAspect.class);
    
	// 获取事务源
    @Autowired
    private DataSourceTransactionManager dataSourceTransactionManager;
    
    private TransactionStatus transactionStatus;
    
	/**
	 * 定义切入点
	 */
	@Pointcut("execution(* com.cluo.mall.miaosha.service.**.*.*(..))")
	public void doAdvice() {
		
	}
	
	/**
	 * 环绕通知
	 * @param joinPoint
	 * @throws Throwable 
	 */
	@Around("doAdvice()")
	public void around(ProceedingJoinPoint joinPoint) throws Throwable {
		//获取目标方法的请求参数
		Object[] args = joinPoint.getArgs();
		//获取代理对象的方法名称
		String methodName = joinPoint.getSignature().getName();
		//获取目标对象
		Class<? extends Object> classTarget = joinPoint.getTarget().getClass();
		//获取目标对象类型
		Class<?>[] classType = ((MethodSignature) joinPoint.getSignature()).getParameterTypes();
		//获取目标对象的方法
		Method objMethod = classTarget.getMethod(methodName, classType);
		//获取目标方法上的事务注解
		Transactional transactional = objMethod.getDeclaredAnnotation(Transactional.class);
		if(transactional!=null) {
			//开启事务
			logger.info("===>开启事务");
			transactionStatus = dataSourceTransactionManager.getTransaction(new DefaultTransactionAttribute());
			//调用目标代理对象方法
			joinPoint.proceed();
			if(transactionStatus!=null) {
			   //提交事务
			   logger.info("===>提交事务");	
			   dataSourceTransactionManager.commit(transactionStatus);
			}
		}
		
	}
	
	/**
	 * 异常通知
	 */
	@AfterThrowing("doAdvice()")
	public void afterThrowing() {
		if(dataSourceTransactionManager != null) dataSourceTransactionManager.rollback(transactionStatus);
	}
	
}
