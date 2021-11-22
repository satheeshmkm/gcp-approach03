package com.sck.gcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages ={"com.sck"})
public class GcpApproach03Application {

	public static void main(String[] args) {
		SpringApplication.run(GcpApproach03Application.class, args);
	}
}
