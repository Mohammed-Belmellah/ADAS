package com.example.ADAS_App;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdasAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdasAppApplication.class, args);
	}

}
