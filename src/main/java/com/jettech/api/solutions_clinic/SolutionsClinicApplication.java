package com.jettech.api.solutions_clinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SolutionsClinicApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolutionsClinicApplication.class, args);
	}

}
