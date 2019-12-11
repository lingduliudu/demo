package com.example.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController
public class TestController {
	private static Logger logger = LogManager.getLogger(TestController.class);

	@RequestMapping("/1")
	public String abc() {
		logger.error("====test=====");
		return "12";
	}
}
