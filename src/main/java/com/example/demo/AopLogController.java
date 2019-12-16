package com.example.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;



/**
 * 功能说明：controller的通用日志处理类
 * 修改人：
 * 修改内容：
 * 修改注意点：
 */
@Aspect
@Component
public class AopLogController {
	
	private static Logger logger = LogManager.getLogger(AopLogController.class);
	/**
	 * 功能说明：切入点
	 * 修改人：
	 * 修改内容：
	 * 修改注意点：
	 */
	@Pointcut("@annotation(com.example.demo.LogComment)")
	public void cutOffPoint() {}
	/**
	 * 功能说明：请求前
	 * 修改人：
	 * 修改内容：
	 * 修改注意点：
	 */
	@Before("cutOffPoint()")
	public void before(JoinPoint joinPoint) {
		try {
			// 获得对象名
			String classType = joinPoint.getTarget().getClass().getName();
			Class<?> clazz =  Class.forName(classType);;
			String clazzName = clazz.getName();
			// 获得方法名
			String methodName = joinPoint.getSignature().getName();
			MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
			LogComment comment = methodSignature.getMethod().getDeclaredAnnotation(LogComment.class);
			String desc = "";
			if(comment!=null) {
				desc = comment.desc();
			}
			logger.info("---"+desc+"---className:"+clazzName+"---methodName:"+methodName+"---");
			// 获取参数
			Object[] objs = joinPoint.getArgs();
			if (objs != null && objs.length > 0) {
				for (int i = 0; i < objs.length; i++) {
					try {
						logger.info("---" + desc + "---className:" + clazzName + "---methodName:" + methodName
								+ "---params:" + JSONObject.toJSONString(objs[i]));
					} catch (Exception e) {
						logger.error(e.getMessage(),e);
					}
				}
			}
		}	catch (ClassNotFoundException e) {
			logger.error(e.getMessage(),e);
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
			Class<?> clazz =  Class.forName(classType);;
			String clazzName = clazz.getName();
			// 获得方法名
			String methodName = joinPoint.getSignature().getName();
			MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
			LogComment comment = methodSignature.getMethod().getDeclaredAnnotation(LogComment.class);
			String desc = "";
			if(comment!=null) {
				desc = comment.desc();
			}
			try {
				// 获取参数
				logger.info("---" + desc + "---className:" + clazzName + "---methodName:" + methodName + ",result:"
						+ JSONObject.toJSONString(rvt));
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}	catch (ClassNotFoundException e) {
			logger.error(e.getMessage(),e);
		}
	}
}
