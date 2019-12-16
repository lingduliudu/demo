package com.example.demo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SyncSign {
	public String desc() default ""; // 描述
	public String value() default ""; // 同步值
	public long expire_time() default 5L; // 超时时间 0 无限 单位秒
	public long request_time() default 10L; // 请求超时时间 0 无限等待 单位秒
	public boolean exception_continue() default false;
	public String bindParams() default "";// 可用于特定参数处理 绑定参数名 method (String a,String b) 若绑定b 则 写 bindParams="b"
	
	
}
