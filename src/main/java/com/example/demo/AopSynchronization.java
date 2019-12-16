package com.example.demo;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;

@Aspect
@Component
public class AopSynchronization {
	
	private static Logger logger = LogManager.getLogger(AopSynchronization.class);

	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	private String SYNC_PRE_KEY="sync:";
	/**
	 * 功能说明：切入点
	 * 修改人：
	 * 修改内容：
	 * 修改注意点：
	 */
	@Pointcut("@annotation(com.example.demo.SyncSign)")
	public void cutOffPoint() {}
	/**
	 * 功能说明：请求前
	 * 修改人：
	 * 修改内容：
	 * 修改注意点：
	 */
	@Before("cutOffPoint()")
	public void before(JoinPoint joinPoint) {
		boolean exception_continue = false;
		try {
			// 获得对象名
			String classType = joinPoint.getTarget().getClass().getName();
			Class<?> clazz =  Class.forName(classType);;
			String clazzName = clazz.getName();
			// 获得方法名
			String methodName = joinPoint.getSignature().getName();
			MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
			SyncSign comment = methodSignature.getMethod().getDeclaredAnnotation(SyncSign.class);
			String desc =comment.desc();
			long expire_time= comment.expire_time();
			long request_time = comment.request_time();
			long request_timeout = request_time*1000;
			exception_continue = comment.exception_continue(); 
			String bindParams = comment.bindParams();
			// 是否指定了前缀, 如果没有则直接 使用uuid 自定义
			String valueAfter = "";
			// 获取 方法的参数名
			String[] params =  methodSignature.getParameterNames();
			Object[] objs = joinPoint.getArgs();
			if(params!=null && params.length>0) {
				// 如果绑定参数不是空
				if(!StringUtils.isEmpty(bindParams)) {
					for(int i=0;i<params.length;i++) {
						String paramName = params[i];
						if(paramName.equals(bindParams)) {
							// 二次循环获取参数值数据
							if(objs[i]!=null) {
								valueAfter = objs[i].toString();
							}
							break;
						}
					}
				}
			}
			boolean redisResult = false;
			long startTime = System.currentTimeMillis();
			String extraDesc = "";
			while(!redisResult) {
				if(expire_time>0) {
					redisResult = redisTemplate.opsForValue().setIfAbsent(SYNC_PRE_KEY+comment.value()+valueAfter, "1", expire_time, TimeUnit.SECONDS);
				}else {
					redisResult = redisTemplate.opsForValue().setIfAbsent(SYNC_PRE_KEY+comment.value()+valueAfter, "1");
				}
				// 超时退出
				if(request_time>0) {
					long endTime = System.currentTimeMillis();
					// 超时时间
					if((endTime-startTime)>request_timeout) {
						extraDesc ="请求超时";
						break;
					}
				}
				// 等待 100 毫秒
				Thread.sleep(100);
			}
			//
			if(redisResult) {
				logger.info("---"+desc+"---className:"+clazzName+"---methodName:"+methodName+"--- redis 同步锁成功! key:"+SYNC_PRE_KEY+comment.value()+valueAfter);
			}
				// 如果redis 加锁失败了
			if(!redisResult) {
				logger.info("---"+desc+"---className:"+clazzName+"---methodName:"+methodName+"--- redis 同步锁失败! key:"+SYNC_PRE_KEY+comment.value()+valueAfter);
				//
				if(!exception_continue) {
					throw new RuntimeException(extraDesc+"redis 同步锁失败! key:"+SYNC_PRE_KEY+comment.value()+valueAfter);
				}
			}
			
			// 获取参数
		}	catch (Exception e) {
			logger.error(e.getMessage(),e);
			// 如果继续执行则不能抛出异常
			if(!exception_continue) {
				throw new RuntimeException(e);
			}
		}
	}
	/**
	 * 功能说明：请求后
	 * 修改人：
	 * 修改内容：
	 * 修改注意点：
	 */
	@AfterReturning(returning="rvt", pointcut="cutOffPoint()")
	public void after(JoinPoint joinPoint,Object rvt) {
		try {
			// 获得对象名
			String classType = joinPoint.getTarget().getClass().getName();
			Class<?> clazz =  Class.forName(classType);
			String clazzName = clazz.getName();
			String methodName = joinPoint.getSignature().getName();
			// 获得方法名
			MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
			SyncSign comment = methodSignature.getMethod().getDeclaredAnnotation(SyncSign.class);
			String desc = comment.desc();
			String bindParams = comment.bindParams();
			// 是否指定了前缀, 如果没有则直接 使用uuid 自定义
			String valueAfter = "";
			// 获取 方法的参数名
			String[] params =  methodSignature.getParameterNames();
			Object[] objs = joinPoint.getArgs();
			if(params!=null && params.length>0) {
				// 如果绑定参数不是空
				if(!StringUtils.isEmpty(bindParams)) {
					for(int i=0;i<params.length;i++) {
						String paramName = params[i];
						if(paramName.equals(bindParams)) {
							// 二次循环获取参数值数据
							if(objs[i]!=null) {
								valueAfter = objs[i].toString();
							}
							break;
						}
					}
				}
			}
			boolean result  = redisTemplate.delete(SYNC_PRE_KEY+comment.value()+valueAfter);
			if(result) {
				logger.info("---"+desc+"---className:"+clazzName+"---methodName:"+methodName+"---redis 同步锁 删除成功! key:"+SYNC_PRE_KEY+comment.value()+valueAfter);
			}else {
				logger.info("---"+desc+"---className:"+clazzName+"---methodName:"+methodName+"---redis 同步锁 删除失败! key:"+SYNC_PRE_KEY+comment.value()+valueAfter);
			}
			// 获取参数
		}	catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}
}
