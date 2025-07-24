package com.elms.leave_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@EnableDiscoveryClient
@EnableFeignClients
//@ComponentScan(basePackages = {
//		"com.elms.leave-service.feignClient"
//})
public class LeaveServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeaveServiceApplication.class, args);
	}

}
