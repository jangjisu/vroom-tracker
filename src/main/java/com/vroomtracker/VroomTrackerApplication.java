package com.vroomtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class VroomTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(VroomTrackerApplication.class, args);
	}

}
