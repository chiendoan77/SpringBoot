package com.example.soa_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example")
public class SoaProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoaProjectApplication.class, args);
	}

}
