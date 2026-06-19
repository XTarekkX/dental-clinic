package com.example.clinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ClinicApplication {

	public static void main(String[] args) {

		SpringApplication.run(ClinicApplication.class, args);
	}

}
