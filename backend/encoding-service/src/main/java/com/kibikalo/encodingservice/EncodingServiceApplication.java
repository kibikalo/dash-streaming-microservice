package com.kibikalo.encodingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EncodingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EncodingServiceApplication.class, args);
	}

}
