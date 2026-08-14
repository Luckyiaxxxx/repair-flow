package com.repair;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RepairFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(RepairFlowApplication.class, args);
	}

}
