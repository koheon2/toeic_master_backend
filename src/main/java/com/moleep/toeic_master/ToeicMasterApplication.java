package com.moleep.toeic_master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ToeicMasterApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToeicMasterApplication.class, args);
	}

}
