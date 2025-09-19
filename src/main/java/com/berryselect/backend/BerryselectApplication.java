package com.berryselect.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BerryselectApplication {

	public static void main(String[] args) {
		SpringApplication.run(BerryselectApplication.class, args);
	}

}
