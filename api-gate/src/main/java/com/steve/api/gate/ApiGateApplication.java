package com.steve.api.gate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDiscoveryClient
@SpringBootApplication
public class ApiGateApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGateApplication.class, args);
	}

}
