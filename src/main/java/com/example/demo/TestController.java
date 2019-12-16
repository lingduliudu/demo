package com.example.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController
public class TestController {
	private static Logger logger = LogManager.getLogger(TestController.class);

	@SyncSign(value="lock",bindParams = "abc",desc = "同步abc",expire_time = 100L,request_time = 10L)
	@RequestMapping("/1")
	public String abc(String abc) {
		logger.error("====test=====");
		try {
			Thread.sleep(20000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "12";
	}
}
